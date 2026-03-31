package com.survey.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "result_profiles")
public class ResultProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    /** 관리자가 구분할 수 있는 키(조건이 이 키를 참조) */
    @Column(name = "profile_key", nullable = false)
    private String key;

    @Column(nullable = false)
    private String skinTypeLabel;

    @Column(length = 2000)
    private String statusText;

    @Column(nullable = false)
    private String productSetTitle;

    @Lob
    @Column(name = "products_json", nullable = false)
    private String productsJson;

    @Column(nullable = false)
    private String couponButtonText;

    private String couponUrl;

    /**
     * 아임웹에서 발급/적용에 사용되는 쿠폰 템플릿 코드.
     * (지정발행/쿠폰코드 발급 등 연동 방식에 따라 의미가 달라질 수 있어요.)
     */
    @Column(name = "coupon_code", length = 255)
    private String couponCode;

    private int priority = 100;

    private boolean defaultProfile;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultCondition> conditions = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Survey getSurvey() { return survey; }
    public void setSurvey(Survey survey) { this.survey = survey; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getSkinTypeLabel() { return skinTypeLabel; }
    public void setSkinTypeLabel(String skinTypeLabel) { this.skinTypeLabel = skinTypeLabel; }
    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
    public String getProductSetTitle() { return productSetTitle; }
    public void setProductSetTitle(String productSetTitle) { this.productSetTitle = productSetTitle; }
    public String getProductsJson() { return productsJson; }
    public void setProductsJson(String productsJson) { this.productsJson = productsJson; }
    public String getCouponButtonText() { return couponButtonText; }
    public void setCouponButtonText(String couponButtonText) { this.couponButtonText = couponButtonText; }
    public String getCouponUrl() { return couponUrl; }
    public void setCouponUrl(String couponUrl) { this.couponUrl = couponUrl; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public boolean isDefaultProfile() { return defaultProfile; }
    public void setDefaultProfile(boolean defaultProfile) { this.defaultProfile = defaultProfile; }
    public List<ResultCondition> getConditions() { return conditions; }
    public void setConditions(List<ResultCondition> conditions) { this.conditions = conditions; }
}

