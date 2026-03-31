package com.survey.dto;

import java.util.List;
import java.util.Map;

public class QuestionStatDto {

    private Long questionId;
    private String questionText;
    private String type;
    /** SINGLE_CHOICE, MULTIPLE_CHOICE: option -> count */
    private Map<String, Long> optionCounts;
    /** SHORT_TEXT, LONG_TEXT: list of answer texts */
    private List<String> textAnswers;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Map<String, Long> getOptionCounts() { return optionCounts; }
    public void setOptionCounts(Map<String, Long> optionCounts) { this.optionCounts = optionCounts; }
    public List<String> getTextAnswers() { return textAnswers; }
    public void setTextAnswers(List<String> textAnswers) { this.textAnswers = textAnswers; }
}
