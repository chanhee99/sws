package com.survey.controller;

import com.survey.dto.ResponseDetailDto;
import com.survey.dto.ResponseListItemDto;
import com.survey.dto.StatisticsDto;
import com.survey.dto.ResultConfigDto;
import com.survey.dto.ResultConfigUpsertRequest;
import com.survey.dto.SurveyDto;
import com.survey.dto.UpsertSurveyRequest;
import com.survey.service.ResultConfigService;
import com.survey.service.AdminSurveyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminSurveyService adminSurveyService;
    private final ResultConfigService resultConfigService;

    public AdminController(AdminSurveyService adminSurveyService, ResultConfigService resultConfigService) {
        this.adminSurveyService = adminSurveyService;
        this.resultConfigService = resultConfigService;
    }

    @GetMapping("/responses")
    public Page<ResponseListItemDto> getResponses(
            @RequestParam(required = false) Long surveyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminSurveyService.getResponses(surveyId, page, size);
    }

    @GetMapping("/responses/{id}")
    public ResponseEntity<ResponseDetailDto> getResponseDetail(@PathVariable Long id) {
        return adminSurveyService.getResponseDetail(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/statistics")
    public List<StatisticsDto> getStatistics() {
        return adminSurveyService.getStatistics();
    }

    @GetMapping("/statistics/{surveyId}")
    public ResponseEntity<StatisticsDto> getStatistics(@PathVariable Long surveyId) {
        return adminSurveyService.getStatistics(surveyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/surveys")
    public List<SurveyDto> listAllSurveys() {
        return adminSurveyService.listAllSurveys();
    }

    @PostMapping("/surveys")
    public SurveyDto createSurvey(@Valid @RequestBody UpsertSurveyRequest request) {
        return adminSurveyService.createSurvey(request);
    }

    @PutMapping("/surveys/{id}")
    public ResponseEntity<SurveyDto> updateSurvey(@PathVariable Long id, @Valid @RequestBody UpsertSurveyRequest request) {
        return adminSurveyService.updateSurvey(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/surveys/{id}/active")
    public ResponseEntity<SurveyDto> setSurveyActive(@PathVariable Long id, @RequestParam boolean active) {
        return adminSurveyService.setSurveyActive(id, active)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/surveys/{surveyId}/result-config")
    public ResponseEntity<ResultConfigDto> getResultConfig(@PathVariable Long surveyId) {
        return ResponseEntity.ok(resultConfigService.getConfig(surveyId));
    }

    @PutMapping("/surveys/{surveyId}/result-config")
    public ResponseEntity<Void> upsertResultConfig(
            @PathVariable Long surveyId,
            @Valid @RequestBody ResultConfigUpsertRequest request) {
        resultConfigService.upsertConfig(surveyId, request);
        return ResponseEntity.ok().build();
    }
}
