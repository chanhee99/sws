package com.survey.dto;

import java.time.Instant;
import java.util.List;

public class ResponseDetailDto {

    private Long id;
    private Long surveyId;
    private String surveyTitle;
    private Instant submittedAt;
    private String respondentId;
    private List<AnswerDetailDto> answers;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSurveyId() { return surveyId; }
    public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }
    public String getSurveyTitle() { return surveyTitle; }
    public void setSurveyTitle(String surveyTitle) { this.surveyTitle = surveyTitle; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public String getRespondentId() { return respondentId; }
    public void setRespondentId(String respondentId) { this.respondentId = respondentId; }
    public List<AnswerDetailDto> getAnswers() { return answers; }
    public void setAnswers(List<AnswerDetailDto> answers) { this.answers = answers; }
}
