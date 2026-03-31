package com.survey.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.survey.imweb.ImwebAdminService;
import com.survey.imweb.ImwebProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/imweb")
public class ImwebAdminController {

    private final ImwebAdminService imwebAdminService;
    private final ImwebProperties imwebProperties;

    public ImwebAdminController(ImwebAdminService imwebAdminService, ImwebProperties imwebProperties) {
        this.imwebAdminService = imwebAdminService;
        this.imwebProperties = imwebProperties;
    }

    private ResponseEntity<JsonNode> configNotReady() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode()
                        .put("success", false)
                        .put("message", "IMWEB API Key/Secret이 설정되지 않았습니다. backend/.env를 확인해 주세요.")
        );
    }

    @GetMapping("/members/{id}")
    public ResponseEntity<JsonNode> getMember(
            @PathVariable String id,
            @RequestParam(defaultValue = "uid") String type
    ) {
        if (!imwebProperties.isConfigured()) return configNotReady();
        return ResponseEntity.ok(imwebAdminService.getMember(id, type));
    }

    @GetMapping("/products")
    public ResponseEntity<JsonNode> listProducts(
            @RequestParam(defaultValue = "1") int offset,
            @RequestParam(defaultValue = "25") int limit,
            @RequestParam(defaultValue = "latest") String version
    ) {
        if (!imwebProperties.isConfigured()) return configNotReady();
        return ResponseEntity.ok(imwebAdminService.listProducts(offset, limit, version));
    }

    @GetMapping("/coupons/{couponCode}")
    public ResponseEntity<JsonNode> getCoupon(
            @PathVariable String couponCode
    ) {
        if (!imwebProperties.isConfigured()) return configNotReady();
        return ResponseEntity.ok(imwebAdminService.getCoupon(couponCode));
    }

    @GetMapping("/coupons")
    public ResponseEntity<JsonNode> listCoupons() {
        if (!imwebProperties.isConfigured()) return configNotReady();
        return ResponseEntity.ok(imwebAdminService.listCoupons());
    }

    @GetMapping("/issue-coupons/{issuedCouponCode}")
    public ResponseEntity<JsonNode> getIssuedCoupon(
            @PathVariable String issuedCouponCode
    ) {
        if (!imwebProperties.isConfigured()) return configNotReady();
        return ResponseEntity.ok(imwebAdminService.getIssuedCoupon(issuedCouponCode));
    }

    /**
     * 실제 “특정 회원에게 issued 지급(POST)” 엔드포인트는 iMweb 문서에서 명확한 공개 POST를 찾기 어려웠습니다.
     * 대신 클라이언트에서 필요한 문서/엔드포인트를 주시면 바로 이 메서드에 연결해드릴 수 있습니다.
     */
    @PostMapping("/coupons/issue")
    public ResponseEntity<JsonNode> issueCouponPlaceholder(@RequestBody Map<String, Object> body) {
        if (!imwebProperties.isConfigured()) return configNotReady();

        String memberId = body.get("memberId") == null ? null : String.valueOf(body.get("memberId"));
        String couponCode = body.get("couponCode") == null ? null : String.valueOf(body.get("couponCode"));

        if (memberId == null || memberId.isBlank() || couponCode == null || couponCode.isBlank()) {
            return ResponseEntity.badRequest().body(
                    com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode()
                            .put("success", false)
                            .put("message", "memberId 와 couponCode 를 입력해 주세요.")
            );
        }

        return ResponseEntity.ok(imwebAdminService.issueCouponToMember(memberId, couponCode));
    }
}

