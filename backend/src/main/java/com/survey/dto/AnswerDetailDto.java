package com.survey.dto;

public class AnswerDetailDto {

    private Long questionId;
    private String questionText;
    private String questionType;
    private String value;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
