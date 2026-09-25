package com.newsplatform.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.frontend-url}") String frontendUrls,
            @Value("${app.environment:development}") String environment
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> configuredOrigins = Arrays.stream(frontendUrls.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
        if (configuredOrigins.stream().anyMatch("*"::equals)) {
            throw new IllegalStateException("FRONTEND_URL must contain explicit origins; '*' is not allowed");
        }
        List<String> origins = new java.util.ArrayList<>(configuredOrigins);
        if ("development".equalsIgnoreCase(environment) && !origins.contains("http://127.0.0.1:3000")) {
            origins.add("http://127.0.0.1:3000");
        }
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Request-Id"));
        configuration.setExposedHeaders(List.of("X-Request-Id"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
