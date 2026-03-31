package com.survey.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class SubmitSurveyRequest {

    @NotNull(message = "surveyId is required")
    private Long surveyId;

    /** 아임웹 회원 ID 등 (선택) */
    private String respondentId;

    private List<AnswerInput> answers;

    public Long getSurveyId() { return surveyId; }
    public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }
    public String getRespondentId() { return respondentId; }
    public void setRespondentId(String respondentId) { this.respondentId = respondentId; }
    public List<AnswerInput> getAnswers() { return answers; }
    public void setAnswers(List<AnswerInput> answers) { this.answers = answers; }

    public static class AnswerInput {
        private Long questionId;
        private String value;  // 단일 값 또는 JSON 배열(복수선택)

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}
