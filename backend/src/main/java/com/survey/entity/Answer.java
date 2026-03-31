package com.survey.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private SurveyResponse response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /** 단답/장문: 텍스트, 객관식: 선택한 옵션 텍스트 또는 JSON 배열 */
    @Column(name = "answer_value", length = 2000)
    private String value;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SurveyResponse getResponse() { return response; }
    public void setResponse(SurveyResponse response) { this.response = response; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
