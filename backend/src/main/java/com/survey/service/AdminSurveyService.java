package com.survey.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.dto.*;
import com.survey.entity.Answer;
import com.survey.entity.Question;
import com.survey.entity.Survey;
import com.survey.entity.SurveyResponse;
import com.survey.repository.SurveyRepository;
import com.survey.repository.SurveyResponseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class AdminSurveyService {

    private static final int MAX_TEXT_ANSWERS = 500;

    private final SurveyResponseRepository responseRepository;
    private final SurveyRepository surveyRepository;
    private final ObjectMapper objectMapper;

    public AdminSurveyService(SurveyResponseRepository responseRepository,
                              SurveyRepository surveyRepository,
                              ObjectMapper objectMapper) {
        this.responseRepository = responseRepository;
        this.surveyRepository = surveyRepository;
        this.objectMapper = objectMapper;
    }

    public Page<ResponseListItemDto> getResponses(Long surveyId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SurveyResponse> pageResult = surveyId != null
                ? responseRepository.findBySurveyIdOrderBySubmittedAtDesc(surveyId, pageable)
                : responseRepository.findAllByOrderBySubmittedAtDesc(pageable);
        return pageResult.map(this::toListItemDto);
    }

    @Transactional(readOnly = true)
    public Optional<ResponseDetailDto> getResponseDetail(Long id) {
        return responseRepository.findByIdWithAnswersAndQuestions(id)
                .map(this::toDetailDto);
    }

    public List<StatisticsDto> getStatistics() {
        List<Survey> surveys = surveyRepository.findAllWithQuestions();
        List<StatisticsDto> result = new ArrayList<>();
        for (Survey survey : surveys) {
            result.add(getStatisticsForSurvey(survey));
        }
        return result;
    }

    public Optional<StatisticsDto> getStatistics(Long surveyId) {
        return surveyRepository.findById(surveyId)
                .map(this::getStatisticsForSurvey);
    }

    public List<SurveyDto> listAllSurveys() {
        return surveyRepository.findAllWithQuestions().stream()
                .map(this::toSurveyDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SurveyDto createSurvey(UpsertSurveyRequest request) {
        Survey survey = new Survey();
        applyUpsert(survey, request);
        surveyRepository.save(survey);
        return toSurveyDto(survey);
    }

    @Transactional
    public Optional<SurveyDto> updateSurvey(Long id, UpsertSurveyRequest request) {
        return surveyRepository.findById(id).map(existing -> {
            applyUpsert(existing, request);
            surveyRepository.save(existing);
            return toSurveyDto(existing);
        });
    }

    @Transactional
    public Optional<SurveyDto> setSurveyActive(Long id, boolean active) {
        return surveyRepository.findById(id).map(existing -> {
            existing.setActive(active);
            existing.setUpdatedAt(Instant.now());
            surveyRepository.save(existing);
            return toSurveyDto(existing);
        });
    }

    private StatisticsDto getStatisticsForSurvey(Survey survey) {
        StatisticsDto dto = new StatisticsDto();
        dto.setSurveyId(survey.getId());
        dto.setSurveyTitle(survey.getTitle());
        List<SurveyResponse> responses = responseRepository.findBySurveyIdWithAnswersAndQuestions(survey.getId());
        dto.setTotalResponses(responses.size());

        List<Question> questions = survey.getQuestions().stream()
                .sorted(Comparator.comparingInt(Question::getSortOrder))
                .collect(Collectors.toList());

        List<QuestionStatDto> questionStats = new ArrayList<>();
        for (Question q : questions) {
            QuestionStatDto qs = new QuestionStatDto();
            qs.setQuestionId(q.getId());
            qs.setQuestionText(q.getText());
            qs.setType(q.getType());

            List<Answer> answersForQuestion = responses.stream()
                    .flatMap(r -> r.getAnswers().stream())
                    .filter(a -> a.getQuestion().getId().equals(q.getId()))
                    .collect(Collectors.toList());

            switch (q.getType()) {
                case "SINGLE_CHOICE":
                case "MULTIPLE_CHOICE":
                    Map<String, Long> counts = new HashMap<>();
                    if (q.getOptions() != null) {
                        for (String opt : q.getOptions()) {
                            counts.put(opt, 0L);
                        }
                    }
                    for (Answer a : answersForQuestion) {
                        if (a.getValue() == null) continue;
                        if ("MULTIPLE_CHOICE".equals(q.getType())) {
                            try {
                                List<String> selected = objectMapper.readValue(a.getValue(), new TypeReference<List<String>>() {});
                                if (selected != null) {
                                    for (String s : selected) {
                                        counts.merge(s, 1L, Long::sum);
                                    }
                                }
                            } catch (Exception ignored) {
                                counts.merge(a.getValue(), 1L, Long::sum);
                            }
                        } else {
                            counts.merge(a.getValue(), 1L, Long::sum);
                        }
                    }
                    qs.setOptionCounts(counts);
                    break;
                case "SHORT_TEXT":
                case "LONG_TEXT":
                    List<String> texts = answersForQuestion.stream()
                            .map(Answer::getValue)
                            .filter(Objects::nonNull)
                            .limit(MAX_TEXT_ANSWERS)
                            .collect(Collectors.toList());
                    qs.setTextAnswers(texts);
                    break;
                default:
                    break;
            }
            questionStats.add(qs);
        }
        dto.setQuestionStats(questionStats);
        return dto;
    }

    private void applyUpsert(Survey survey, UpsertSurveyRequest request) {
        survey.setTitle(request.getTitle());
        survey.setDescription(request.getDescription());
        survey.setActive(Boolean.TRUE.equals(request.getActive()));
        survey.setUpdatedAt(Instant.now());
        if (survey.getCreatedAt() == null) {
            survey.setCreatedAt(Instant.now());
        }

        // Replace questions to keep things simple (orphanRemoval=true)
        survey.getQuestions().clear();
        if (request.getQuestions() != null) {
            List<UpsertSurveyRequest.QuestionInput> inputs = new ArrayList<>(request.getQuestions());
            inputs.sort(Comparator.comparingInt(q -> q.getSortOrder() == null ? 0 : q.getSortOrder()));
            int i = 0;
            for (UpsertSurveyRequest.QuestionInput qi : inputs) {
                Question q = new Question();
                q.setSurvey(survey);
                q.setText(qi.getText());
                q.setType(qi.getType());
                q.setOptions(qi.getOptions() == null ? List.of() : qi.getOptions());
                q.setRequired(Boolean.TRUE.equals(qi.getRequired()));
                q.setSortOrder(qi.getSortOrder() == null ? i : qi.getSortOrder());
                survey.getQuestions().add(q);
                i++;
            }
        }
    }

    private SurveyDto toSurveyDto(Survey s) {
        SurveyDto dto = new SurveyDto();
        dto.setId(s.getId());
        dto.setTitle(s.getTitle());
        dto.setDescription(s.getDescription());
        dto.setActive(s.isActive());
        dto.setCreatedAt(s.getCreatedAt());
        dto.setQuestions(s.getQuestions().stream().map(this::toQuestionDto).collect(Collectors.toList()));
        return dto;
    }

    private QuestionDto toQuestionDto(Question q) {
        QuestionDto dto = new QuestionDto();
        dto.setId(q.getId());
        dto.setText(q.getText());
        dto.setType(q.getType());
        dto.setOptions(q.getOptions());
        dto.setRequired(q.isRequired());
        dto.setSortOrder(q.getSortOrder());
        return dto;
    }

    private ResponseListItemDto toListItemDto(SurveyResponse r) {
        ResponseListItemDto dto = new ResponseListItemDto();
        dto.setId(r.getId());
        dto.setSurveyId(r.getSurvey().getId());
        dto.setSurveyTitle(r.getSurvey().getTitle());
        dto.setSubmittedAt(r.getSubmittedAt());
        dto.setRespondentId(r.getRespondentId());
        return dto;
    }

    private ResponseDetailDto toDetailDto(SurveyResponse r) {
        ResponseDetailDto dto = new ResponseDetailDto();
        dto.setId(r.getId());
        dto.setSurveyId(r.getSurvey().getId());
        dto.setSurveyTitle(r.getSurvey().getTitle());
        dto.setSubmittedAt(r.getSubmittedAt());
        dto.setRespondentId(r.getRespondentId());
        dto.setAnswers(r.getAnswers().stream()
                .sorted(Comparator.comparingInt(a -> a.getQuestion().getSortOrder()))
                .map(this::toAnswerDetailDto)
                .collect(Collectors.toList()));
        return dto;
    }

    private AnswerDetailDto toAnswerDetailDto(Answer a) {
        AnswerDetailDto dto = new AnswerDetailDto();
        dto.setQuestionId(a.getQuestion().getId());
        dto.setQuestionText(a.getQuestion().getText());
        dto.setQuestionType(a.getQuestion().getType());
        dto.setValue(a.getValue());
        return dto;
    }
}
