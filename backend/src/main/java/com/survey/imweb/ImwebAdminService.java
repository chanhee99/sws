package com.survey.imweb;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ImwebAdminService {

    private final ImwebClient imwebClient;
    private final ImwebProperties properties;

    public ImwebAdminService(ImwebClient imwebClient, ImwebProperties properties) {
        this.imwebClient = imwebClient;
        this.properties = properties;
    }

    public JsonNode getMember(String id, String type) {
        // iMweb 문서상 member_code/uid 모두 같은 경로 파라미터 구조를 사용합니다.
        // type은 추후 로그/검증용으로만 유지합니다.
        String url = "/v2/member/members/" + id;
        return imwebClient.getJsonWithFullPath(url);
    }

    public JsonNode listProducts(int offset, int limit, String version) {
        Map<String, Object> body = new HashMap<>();
        body.put("version", version == null || version.isBlank() ? "latest" : version);
        body.put("offset", offset);
        body.put("limit", limit);

        String url = "/v2/shop/products";
        return imwebClient.getJsonWithBodyFullPath(url, body);
    }

    public JsonNode listCoupons() {
        String url = "/v2/shop/coupons";
        return imwebClient.getJsonWithFullPath(url);
    }

    public JsonNode getCoupon(String couponCode) {
        String url = "/v2/shop/coupons/" + couponCode;
        return imwebClient.getJsonWithFullPath(url);
    }

    public JsonNode getIssuedCoupon(String issuedCouponCode) {
        String url = "/v2/shop/issue-coupons/" + issuedCouponCode;
        return imwebClient.getJsonWithFullPath(url);
    }

    public JsonNode issueCouponToMember(String memberId, String couponCode) {
        // iMweb 쿠폰 발급 방식은 사이트 설정/버전에 따라 요청 형식이 달라질 수 있어
        // env로 path/필드명을 조절할 수 있게 구성합니다.
        Map<String, Object> body = new HashMap<>();
        body.put("version", properties.getApiVersion());
        body.put(properties.getCouponIssueMemberKey(), memberId);
        body.put(properties.getCouponIssueCouponCodeKey(), couponCode);

        String path = properties.getCouponIssuePath();
        return imwebClient.postJsonWithBodyFullPath(path, body);
    }
}

