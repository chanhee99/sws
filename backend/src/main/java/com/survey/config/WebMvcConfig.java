package com.survey.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ImwebAdminAuthInterceptor imwebAdminAuthInterceptor;

    public WebMvcConfig(ImwebAdminAuthInterceptor imwebAdminAuthInterceptor) {
        this.imwebAdminAuthInterceptor = imwebAdminAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(imwebAdminAuthInterceptor)
                .addPathPatterns("/api/admin/**");
    }
}
