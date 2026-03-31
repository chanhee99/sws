package com.survey.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "result_conditions")
public class ResultCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private ResultProfile profile;

    /** 매칭 대상 문항 */
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /** 객관식 선택지(문자열)와 EXACT 매칭 */
    @Column(name = "match_value", length = 500, nullable = false)
    private String matchValue;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ResultProfile getProfile() { return profile; }
    public void setProfile(ResultProfile profile) { this.profile = profile; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getMatchValue() { return matchValue; }
    public void setMatchValue(String matchValue) { this.matchValue = matchValue; }
}

