package com.survey.imweb;

import java.util.Map;

public class ImwebProperties {

    private final String apiKey;
    private final String apiSecret;
    private final String baseUrl;
    private final String couponIssuePath;
    private final String couponIssueMemberKey;
    private final String couponIssueCouponCodeKey;
    private final String apiVersion;

    public ImwebProperties() {
        Map<String, String> envFile = DotEnvLoader.load();

        this.apiKey = firstNonBlank(
                System.getenv("IMWEB_API_KEY"),
                envFile.get("IMWEB_API_KEY")
        );
        this.apiSecret = firstNonBlank(
                System.getenv("IMWEB_API_SECRET"),
                envFile.get("IMWEB_API_SECRET")
        );
        this.baseUrl = firstNonBlank(
                System.getenv("IMWEB_API_BASE"),
                envFile.get("IMWEB_API_BASE"),
                "https://api.imweb.me"
        );

        this.couponIssuePath = firstNonBlank(
                System.getenv("IMWEB_COUPON_ISSUE_PATH"),
                envFile.get("IMWEB_COUPON_ISSUE_PATH"),
                "/v2/shop/coupons/issue"
        );
        this.couponIssueMemberKey = firstNonBlank(
                System.getenv("IMWEB_COUPON_ISSUE_MEMBER_KEY"),
                envFile.get("IMWEB_COUPON_ISSUE_MEMBER_KEY"),
                "memberId"
        );
        this.couponIssueCouponCodeKey = firstNonBlank(
                System.getenv("IMWEB_COUPON_ISSUE_COUPON_CODE_KEY"),
                envFile.get("IMWEB_COUPON_ISSUE_COUPON_CODE_KEY"),
                "couponCode"
        );
        this.apiVersion = firstNonBlank(
                System.getenv("IMWEB_API_VERSION"),
                envFile.get("IMWEB_API_VERSION"),
                "latest"
        );
    }

    public String getApiKey() { return apiKey; }
    public String getApiSecret() { return apiSecret; }
    public String getBaseUrl() { return baseUrl; }
    public String getCouponIssuePath() { return couponIssuePath; }
    public String getCouponIssueMemberKey() { return couponIssueMemberKey; }
    public String getCouponIssueCouponCodeKey() { return couponIssueCouponCodeKey; }
    public String getApiVersion() { return apiVersion; }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && apiSecret != null && !apiSecret.isBlank();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}

