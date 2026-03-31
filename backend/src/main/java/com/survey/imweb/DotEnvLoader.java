package com.survey.imweb;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * backend/.env 형태의 KEY=VALUE 파일을 읽어 Java 코드에서 사용할 수 있게 합니다.
 * (Spring Boot 기본 설정만으로는 .env를 자동으로 읽지 않기 때문에 직접 로드합니다.)
 */
public class DotEnvLoader {

    public static Map<String, String> load() {
        Map<String, String> result = new HashMap<>();

        Path cwdEnv = Path.of(".env");
        Path backendEnv = Path.of("backend", ".env");

        loadIfExists(cwdEnv, result);
        loadIfExists(backendEnv, result);

        return result;
    }

    private static void loadIfExists(Path envPath, Map<String, String> out) {
        if (!Files.exists(envPath)) return;

        try (BufferedReader br = Files.newBufferedReader(envPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                if (trimmed.startsWith("#")) continue;

                int idx = trimmed.indexOf('=');
                if (idx <= 0) continue;

                String key = trimmed.substring(0, idx).trim();
                String value = trimmed.substring(idx + 1).trim();

                // "..." 또는 '...' 제거
                if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }

                out.put(key, value);
            }
        } catch (IOException ignored) {
            // 환경 파일이 깨져있어도 서버가 죽지 않도록 무시합니다.
        }
    }
}

