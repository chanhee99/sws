package com.survey.imweb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

/**
 * 아임웹 REST API 호출 전용 클라이언트(토큰 자동 처리).
 */
public class ImwebClient {

    private final ImwebProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private String cachedAccessToken;
    private Instant accessTokenExpiresAt;

    public ImwebClient(ImwebProperties properties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public JsonNode getJson(String url, HttpEntity<?> entity) {
        ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return readJson(res);
    }

    public JsonNode getJsonWithFullPath(String pathStartingWithSlash) {
        String url = properties.getBaseUrl() + pathStartingWithSlash;
        HttpHeaders headers = new HttpHeaders();
        headers.set("access-token", getAccessToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return getJson(url, entity);
    }

    public JsonNode postJson(String url, HttpEntity<?> entity) {
        ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return readJson(res);
    }

    public JsonNode getJsonWithBody(String url, Object body) {
        // curl에서 GET + -d 를 사용하기 때문에, RestTemplate에서도 HttpEntity로 바디를 실어 보냅니다.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("access-token", getAccessToken());
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return readJson(res);
    }

    public JsonNode getJsonWithBodyFullPath(String pathStartingWithSlash, Object body) {
        String url = properties.getBaseUrl() + pathStartingWithSlash;
        return getJsonWithBody(url, body);
    }

    public JsonNode postJsonWithBodyFullPath(String pathStartingWithSlash, Object body) {
        String url = properties.getBaseUrl() + pathStartingWithSlash;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("access-token", getAccessToken());

        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return readJson(res);
    }

    private JsonNode readJson(ResponseEntity<String> res) {
        try {
            return objectMapper.readTree(res.getBody() == null ? "{}" : res.getBody());
        } catch (Exception e) {
            // 응답이 JSON이 아니면 빈 노드 반환
            return objectMapper.createObjectNode();
        }
    }

    private String getAccessToken() {
        if (cachedAccessToken != null && accessTokenExpiresAt != null && Instant.now().isBefore(accessTokenExpiresAt)) {
            return cachedAccessToken;
        }

        if (!properties.isConfigured()) {
            throw new IllegalStateException("Imweb API Key/Secret이 설정되어 있지 않습니다. backend/.env를 확인해 주세요.");
        }

        String url = properties.getBaseUrl() + "/v2/auth?key=" + properties.getApiKey() + "&secret=" + properties.getApiSecret();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(MediaType.parseMediaTypes("application/json"));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        JsonNode node;
        try {
            node = objectMapper.readTree(res.getBody() == null ? "{}" : res.getBody());
        } catch (Exception e) {
            throw new IllegalStateException("Imweb token 응답을 파싱하지 못했습니다.");
        }

        JsonNode tokenNode = node.get("access_token");
        if (tokenNode == null || tokenNode.asText().isBlank()) {
            throw new IllegalStateException("Imweb token 발급 실패: access_token이 없습니다. msg=" + (node.get("msg") != null ? node.get("msg").asText() : ""));
        }

        this.cachedAccessToken = tokenNode.asText();

        // 만료 시간이 응답에 없으므로 보수적으로 50분 TTL
        // (응답에 expires_in이 있으면 그걸로 바꾸면 됩니다.)
        this.accessTokenExpiresAt = Instant.now().plusSeconds(50L * 60L);

        return cachedAccessToken;
    }
}

