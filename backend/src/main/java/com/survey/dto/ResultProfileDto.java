package com.survey.dto;

import java.util.List;

public class ResultProfileDto {

    private String key;
    private String skinTypeLabel;
    private String statusText;
    private String productSetTitle;
    private List<ResultProductDto> products;
    private String couponButtonText;
    private String couponUrl;
    /** 아임웹 쿠폰 코드(지정발행/쿠폰 발급 등에서 사용되는 템플릿 코드) */
    private String couponCode;
    private int priority;
    private boolean defaultProfile;

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getSkinTypeLabel() { return skinTypeLabel; }
    public void setSkinTypeLabel(String skinTypeLabel) { this.skinTypeLabel = skinTypeLabel; }
    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
    public String getProductSetTitle() { return productSetTitle; }
    public void setProductSetTitle(String productSetTitle) { this.productSetTitle = productSetTitle; }
    public List<ResultProductDto> getProducts() { return products; }
    public void setProducts(List<ResultProductDto> products) { this.products = products; }
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
}

