package com.survey.service;

import com.survey.dto.QuestionDto;
import com.survey.dto.ResultDto;
import com.survey.dto.SubmitSurveyResponseDto;
import com.survey.dto.SubmitSurveyRequest;
import com.survey.dto.SurveyDto;
import com.survey.entity.*;
import com.survey.imweb.ImwebAdminService;
import com.survey.repository.SurveyRepository;
import com.survey.repository.SurveyResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository responseRepository;
    private final ResultConfigService resultConfigService;
    private final ImwebAdminService imwebAdminService;

    public SurveyService(SurveyRepository surveyRepository,
                          SurveyResponseRepository responseRepository,
                          ResultConfigService resultConfigService,
                          ImwebAdminService imwebAdminService) {
        this.surveyRepository = surveyRepository;
        this.responseRepository = responseRepository;
        this.resultConfigService = resultConfigService;
        this.imwebAdminService = imwebAdminService;
    }

    public List<SurveyDto> listActiveSurveys() {
        return surveyRepository.findAllActiveWithQuestions().stream()
                .map(this::toSurveyDto)
                .collect(Collectors.toList());
    }

    public SurveyDto getSurvey(Long id) {
        return surveyRepository.findById(id)
                .map(this::toSurveyDto)
                .orElseThrow(() -> new IllegalArgumentException("Survey not found: " + id));
    }

    @Transactional
    public SubmitSurveyResponseDto submitSurvey(SubmitSurveyRequest request) {
        Survey survey = surveyRepository.findById(request.getSurveyId())
                .orElseThrow(() -> new IllegalArgumentException("Survey not found: " + request.getSurveyId()));

        SurveyResponse response = new SurveyResponse();
        response.setSurvey(survey);
        response.setRespondentId(request.getRespondentId());
        response = responseRepository.save(response);

        if (request.getAnswers() != null) {
            for (SubmitSurveyRequest.AnswerInput input : request.getAnswers()) {
                Question question = survey.getQuestions().stream()
                        .filter(q -> q.getId().equals(input.getQuestionId()))
                        .findFirst()
                        .orElse(null);
                if (question == null) continue;

                Answer answer = new Answer();
                answer.setResponse(response);
                answer.setQuestion(question);
                answer.setValue(input.getValue());
                response.getAnswers().add(answer);
            }
        }

        response = responseRepository.save(response);

        ResultDto result = resultConfigService.computeResult(request.getSurveyId(), request.getAnswers());

        // 아임웹 쿠폰 발급(issued)을 시도합니다.
        // 실제 요청/파라미터는 iMweb 설정/버전에 따라 다를 수 있으므로, 실패해도 설문 제출은 유지합니다.
        try {
            if (request.getRespondentId() != null
                    && result != null
                    && result.getCouponCode() != null
                    && !request.getRespondentId().isBlank()
                    && !result.getCouponCode().isBlank()) {
                imwebAdminService.issueCouponToMember(request.getRespondentId(), result.getCouponCode());
            }
        } catch (Exception ignored) {
            System.err.println("[Imweb coupon issue failed] " + ignored.getMessage());
        }
        SubmitSurveyResponseDto dto = new SubmitSurveyResponseDto();
        dto.setSuccess(true);
        dto.setResponseId(response.getId());
        dto.setMessage("설문이 제출되었습니다.");
        dto.setResult(result);
        return dto;
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
}
