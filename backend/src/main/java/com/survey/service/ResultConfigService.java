package com.survey.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.dto.*;
import com.survey.entity.ResultCondition;
import com.survey.entity.ResultProfile;
import com.survey.repository.ResultConditionRepository;
import com.survey.repository.ResultProfileRepository;
import com.survey.repository.SurveyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResultConfigService {

    private final ResultProfileRepository profileRepository;
    private final ResultConditionRepository conditionRepository;
    private final ObjectMapper objectMapper;
    private final SurveyRepository surveyRepository;

    public ResultConfigService(ResultProfileRepository profileRepository,
                                ResultConditionRepository conditionRepository,
                                ObjectMapper objectMapper,
                                SurveyRepository surveyRepository) {
        this.profileRepository = profileRepository;
        this.conditionRepository = conditionRepository;
        this.objectMapper = objectMapper;
        this.surveyRepository = surveyRepository;
    }

    @Transactional(readOnly = true)
    public ResultConfigDto getConfig(Long surveyId) {
        List<ResultProfile> profiles = profileRepository.findBySurvey_Id(surveyId);
        List<ResultCondition> conditions = conditionRepository.findAllWithProfileBySurveyId(surveyId);

        Map<Long, String> profileIdToKey = profiles.stream()
                .collect(Collectors.toMap(ResultProfile::getId, ResultProfile::getKey));

        ResultConfigDto dto = new ResultConfigDto();
        dto.setProfiles(profiles.stream().map(this::toProfileDto).collect(Collectors.toList()));
        dto.setConditions(conditions.stream().map(c -> {
            ResultConditionDto cd = new ResultConditionDto();
            cd.setQuestionId(c.getQuestionId());
            cd.setMatchValue(c.getMatchValue());
            cd.setProfileKey(profileIdToKey.get(c.getProfile().getId()));
            return cd;
        }).collect(Collectors.toList()));
        return dto;
    }

    @Transactional
    public void upsertConfig(Long surveyId, ResultConfigUpsertRequest request) {
        // 간단히: 기존 설정을 전부 삭제 후 재생성
        conditionRepository.deleteAllByProfile_Survey_Id(surveyId);
        profileRepository.deleteAllBySurvey_Id(surveyId);

        Map<String, ResultProfile> createdByKey = new HashMap<>();
        var surveyRef = surveyRepository.getReferenceById(surveyId);
        for (ResultProfileDto p : request.getProfiles()) {
            ResultProfile profile = new ResultProfile();
            profile.setSurvey(surveyRef);
            profile.setKey(p.getKey());
            profile.setSkinTypeLabel(p.getSkinTypeLabel());
            profile.setStatusText(p.getStatusText());
            profile.setProductSetTitle(p.getProductSetTitle());
            profile.setCouponButtonText(p.getCouponButtonText());
            profile.setCouponUrl(p.getCouponUrl());
            profile.setCouponCode(p.getCouponCode());
            profile.setPriority(p.getPriority());
            profile.setDefaultProfile(p.isDefaultProfile());
            profile.setProductsJson(toProductsJson(p.getProducts()));

            ResultProfile saved = profileRepository.save(profile);
            createdByKey.put(saved.getKey(), saved);
        }

        // 기본 프로필이 없으면 첫 프로필을 default로 지정
        if (createdByKey.values().stream().noneMatch(ResultProfile::isDefaultProfile)) {
            ResultProfile first = createdByKey.values().stream()
                    .sorted(Comparator.comparingInt(ResultProfile::getPriority))
                    .findFirst().orElse(null);
            if (first != null) {
                first.setDefaultProfile(true);
                profileRepository.save(first);
            }
        }

        for (ResultConditionDto c : request.getConditions()) {
            ResultProfile profile = createdByKey.get(c.getProfileKey());
            if (profile == null) continue;

            ResultCondition cond = new ResultCondition();
            cond.setProfile(profile);
            cond.setQuestionId(c.getQuestionId());
            cond.setMatchValue(c.getMatchValue());
            conditionRepository.save(cond);
        }
    }

    @Transactional(readOnly = true)
    public ResultDto computeResult(Long surveyId, List<SubmitSurveyRequest.AnswerInput> answers) {
        List<ResultProfile> profiles = profileRepository.findBySurvey_Id(surveyId);
        List<ResultCondition> conditions = conditionRepository.findAllWithProfileBySurveyId(surveyId);

        Map<Long, String> answerByQuestionId = new HashMap<>();
        if (answers != null) {
            for (SubmitSurveyRequest.AnswerInput a : answers) {
                if (a == null || a.getQuestionId() == null) continue;
                if (a.getValue() == null) continue;
                // 프론트에서 미응답은 ''로 넣을 수 있으니 제거
                if (a.getValue().trim().isEmpty()) continue;
                answerByQuestionId.put(a.getQuestionId(), a.getValue());
            }
        }

        Set<ResultProfile> matchedProfiles = new HashSet<>();
        for (ResultCondition c : conditions) {
            String answerValue = answerByQuestionId.get(c.getQuestionId());
            if (doesMatch(answerValue, c.getMatchValue())) {
                matchedProfiles.add(c.getProfile());
            }
        }

        ResultProfile chosen = null;
        if (!matchedProfiles.isEmpty()) {
            chosen = matchedProfiles.stream()
                    .sorted(Comparator.comparingInt(ResultProfile::getPriority).reversed())
                    .findFirst().orElse(null);
        }
        if (chosen == null) {
            chosen = profiles.stream().filter(ResultProfile::isDefaultProfile).findFirst().orElse(null);
            if (chosen == null) {
                chosen = profiles.stream()
                        .sorted(Comparator.comparingInt(ResultProfile::getPriority).reversed())
                        .findFirst().orElse(null);
            }
        }

        if (chosen == null) return defaultFallbackResult();
        return toResultDto(chosen);
    }

    private boolean doesMatch(String answerValue, String matchValue) {
        if (answerValue == null || matchValue == null) return false;
        try {
            // MULTIPLE_CHOICE 는 JSON 배열로 저장됨
            List<String> arr = objectMapper.readValue(answerValue, new TypeReference<List<String>>() {});
            if (arr != null) return arr.contains(matchValue);
        } catch (Exception ignored) {
            // SINGLE_CHOICE / TEXT: 그냥 문자열 비교
        }
        return answerValue.equals(matchValue);
    }

    private ResultProfileDto toProfileDto(ResultProfile p) {
        ResultProfileDto dto = new ResultProfileDto();
        dto.setKey(p.getKey());
        dto.setSkinTypeLabel(p.getSkinTypeLabel());
        dto.setStatusText(p.getStatusText());
        dto.setProductSetTitle(p.getProductSetTitle());
        dto.setCouponButtonText(p.getCouponButtonText());
        dto.setCouponUrl(p.getCouponUrl());
        dto.setCouponCode(p.getCouponCode());
        dto.setPriority(p.getPriority());
        dto.setDefaultProfile(p.isDefaultProfile());
        dto.setProducts(fromProductsJson(p.getProductsJson()));
        return dto;
    }

    private ResultDto toResultDto(ResultProfile p) {
        ResultDto dto = new ResultDto();
        dto.setSkinType(p.getSkinTypeLabel());
        dto.setStatusText(p.getStatusText());
        dto.setProductSetTitle(p.getProductSetTitle());
        dto.setProducts(fromProductsJson(p.getProductsJson()));
        dto.setCouponButtonText(p.getCouponButtonText());
        dto.setCouponUrl(p.getCouponUrl());
        dto.setCouponCode(p.getCouponCode());
        return dto;
    }

    private List<ResultProductDto> fromProductsJson(String productsJson) {
        if (productsJson == null || productsJson.isBlank()) return List.of();
        try {
            return objectMapper.readValue(productsJson, new TypeReference<List<ResultProductDto>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private String toProductsJson(List<ResultProductDto> products) {
        try {
            return objectMapper.writeValueAsString(products == null ? List.of() : products);
        } catch (Exception e) {
            return "[]";
        }
    }

    private ResultDto defaultFallbackResult() {
        ResultDto dto = new ResultDto();
        dto.setSkinType("복합형");
        dto.setStatusText("설문 결과를 불러오는 중입니다. 관리자 설정을 확인해 주세요.");
        dto.setProductSetTitle("추천 제품 세트");
        dto.setProducts(List.of(
                new ResultProductDto(),
                new ResultProductDto(),
                new ResultProductDto()
        ));
        dto.setCouponButtonText("맞춤 케어 시작하기 (혜택 적용)");
        dto.setCouponUrl(null);
        dto.setCouponCode(null);
        return dto;
    }
}

