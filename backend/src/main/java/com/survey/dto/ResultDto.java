package com.survey.dto;

import java.util.List;

public class ResultDto {

    private String skinType;
    private String statusText;
    private String productSetTitle;
    private List<ResultProductDto> products;
    private String couponButtonText;
    private String couponUrl;
    private String couponCode;

    public String getSkinType() { return skinType; }
    public void setSkinType(String skinType) { this.skinType = skinType; }
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
}

