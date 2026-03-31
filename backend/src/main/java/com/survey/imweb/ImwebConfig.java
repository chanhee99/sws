package com.survey.imweb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ImwebConfig {

    @Bean
    public ImwebProperties imwebProperties() {
        return new ImwebProperties();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ImwebClient imwebClient(ImwebProperties properties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        return new ImwebClient(properties, restTemplate, objectMapper);
    }
}

