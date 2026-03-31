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
 * /api/admin/** 접근 제어.
 * <ul>
 *   <li>{@code app.admin.secret} 이 비어 있고 {@code app.imweb.admin-member-ids} 도 비어 있으면 검사하지 않습니다(로컬 개발).</li>
 *   <li>둘 중 하나라도 설정되면: {@code X-Admin-Secret} 이 일치하거나, {@code X-Imweb-Member-Id} 가 허용 목록에 있으면 통과합니다(OR).</li>
 * </ul>
 */
@Component
public class AdminApiAuthInterceptor implements HandlerInterceptor {

    public static final String HEADER_ADMIN_SECRET = "X-Admin-Secret";
    public static final String HEADER_IMWEB_MEMBER_ID = "X-Imweb-Member-Id";

    private final String adminSecret;
    private final Set<String> imwebAdminIds;
    private final ObjectMapper objectMapper;

    public AdminApiAuthInterceptor(
            @Value("${app.admin.secret:}") String adminSecret,
            @Value("${app.imweb.admin-member-ids:}") String imwebAdminMemberIdsRaw,
            ObjectMapper objectMapper
    ) {
        this.adminSecret = adminSecret == null ? "" : adminSecret.trim();
        this.imwebAdminIds = parseIds(imwebAdminMemberIdsRaw);
        this.objectMapper = objectMapper;
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

        boolean secretConfigured = !adminSecret.isEmpty();
        boolean imwebConfigured = !imwebAdminIds.isEmpty();

        if (!secretConfigured && !imwebConfigured) {
            return true;
        }

        String providedSecret = request.getHeader(HEADER_ADMIN_SECRET);
        if (secretConfigured && adminSecret.equals(providedSecret)) {
            return true;
        }

        String memberId = request.getHeader(HEADER_IMWEB_MEMBER_ID);
        if (imwebConfigured && memberId != null && !memberId.isBlank() && imwebAdminIds.contains(memberId.trim())) {
            return true;
        }

        writeForbidden(response, "관리자 API 접근이 거부되었습니다. X-Admin-Secret 또는 허용된 X-Imweb-Member-Id 가 필요합니다.");
        return false;
    }

    private void writeForbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), java.util.Map.of("message", message));
    }
}
