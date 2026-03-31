package com.survey.dto;

import java.util.List;

public class StatisticsDto {

    private Long surveyId;
    private String surveyTitle;
    private long totalResponses;
    private List<QuestionStatDto> questionStats;

    public Long getSurveyId() { return surveyId; }
    public void setSurveyId(Long surveyId) { this.surveyId = surveyId; }
    public String getSurveyTitle() { return surveyTitle; }
    public void setSurveyTitle(String surveyTitle) { this.surveyTitle = surveyTitle; }
    public long getTotalResponses() { return totalResponses; }
    public void setTotalResponses(long totalResponses) { this.totalResponses = totalResponses; }
    public List<QuestionStatDto> getQuestionStats() { return questionStats; }
    public void setQuestionStats(List<QuestionStatDto> questionStats) { this.questionStats = questionStats; }
}
