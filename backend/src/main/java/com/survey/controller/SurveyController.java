package com.survey.controller;

import com.survey.dto.SubmitSurveyRequest;
import com.survey.dto.SurveyDto;
import com.survey.dto.SubmitSurveyResponseDto;
import com.survey.service.SurveyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SurveyController {

    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    /** 활성 설문 목록 (아임웹 등 외부에서 설문 선택용) */
    @GetMapping("/surveys")
    public List<SurveyDto> listSurveys() {
        return surveyService.listActiveSurveys();
    }

    /** 설문 상세 + 문항 (제출 폼에 사용) */
    @GetMapping("/surveys/{id}")
    public SurveyDto getSurvey(@PathVariable Long id) {
        return surveyService.getSurvey(id);
    }

    /** 설문 제출 */
    @PostMapping("/surveys/submit")
    public ResponseEntity<SubmitSurveyResponseDto> submitSurvey(@Valid @RequestBody SubmitSurveyRequest request) {
        return ResponseEntity.ok(surveyService.submitSurvey(request));
    }
}
