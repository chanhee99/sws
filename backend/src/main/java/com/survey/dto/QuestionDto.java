package com.survey.dto;

import java.util.List;

public class QuestionDto {

    private Long id;
    private String text;
    private String type;  // SINGLE_CHOICE, MULTIPLE_CHOICE, SHORT_TEXT, LONG_TEXT
    private List<String> options;
    private boolean required;
    private int sortOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
