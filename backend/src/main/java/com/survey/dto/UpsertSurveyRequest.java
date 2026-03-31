package com.survey.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class UpsertSurveyRequest {

    @NotBlank(message = "title is required")
    private String title;

    private String description;

    @NotNull(message = "active is required")
    private Boolean active;

    @Valid
    private List<QuestionInput> questions = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public List<QuestionInput> getQuestions() { return questions; }
    public void setQuestions(List<QuestionInput> questions) { this.questions = questions; }

    public static class QuestionInput {
        private Long id; // update 시 사용 (없으면 신규)

        @NotBlank(message = "question.text is required")
        private String text;

        @NotBlank(message = "question.type is required")
        private String type; // SINGLE_CHOICE, MULTIPLE_CHOICE, SHORT_TEXT, LONG_TEXT

        private List<String> options = new ArrayList<>();

        @NotNull(message = "question.required is required")
        private Boolean required;

        private Integer sortOrder = 0;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
        public Boolean getRequired() { return required; }
        public void setRequired(Boolean required) { this.required = required; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }
}

