package com.survey.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 아임웹에 로그인한 "운영자(관리자)"만 관리자 API를 쓰도록 제한합니다.
 * <p>
 * 아임웹 페이지/스크립트에서 로그인 회원 uid(또는 member_code)를
 * {@code X-Imweb-Member-Id} 헤더로 보내고, {@code app.imweb.admin-member-ids}에 포함된 경우만 허용합니다.
 * 목록이 비어 있으면(개발용) 검사를 하지 않습니다.
 */
@Component
public class ImwebAdminAuthInterceptor implements HandlerInterceptor {

    public static final String HEADER_MEMBER_ID = "X-Imweb-Member-Id";

    private final Set<String> adminIds;
    private final ObjectMapper objectMapper;

    public ImwebAdminAuthInterceptor(
            @Value("${app.imweb.admin-member-ids:}") String adminMemberIdsRaw,
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
        this.adminIds = parseIds(adminMemberIdsRaw);
    }

    private static Set<String> parseIds(String raw) {
        Set<String> out = new LinkedHashSet<>();
        if (raw == null || raw.isBlank()) return out;
        for (String p : raw.split(",")) {
            String t = p.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (adminIds.isEmpty()) {
            return true;
        }

        String memberId = request.getHeader(HEADER_MEMBER_ID);
        if (memberId == null || memberId.isBlank()) {
            writeForbidden(response, "아임웹 관리자 연동: " + HEADER_MEMBER_ID + " 헤더가 필요합니다.");
            return false;
        }
        if (!adminIds.contains(memberId.trim())) {
            writeForbidden(response, "관리자 권한이 없습니다.");
            return false;
        }
        return true;
    }

    private void writeForbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), java.util.Map.of("message", message));
    }
}
