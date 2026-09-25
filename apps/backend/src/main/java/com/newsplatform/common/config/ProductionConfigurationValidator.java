package com.newsplatform.common.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Fails fast on the development-only defaults that are unsafe in a production profile. */
@Component
public class ProductionConfigurationValidator {
    private final String environment;
    private final String jwtSecret;
    private final String frontendUrls;
    private final boolean hstsEnabled;
    private final String databasePassword;
    private final String initialAdminPassword;

    public ProductionConfigurationValidator(
            @Value("${app.environment:development}") String environment,
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.frontend-url:http://localhost:3000}") String frontendUrls,
            @Value("${app.security.hsts-enabled:false}") boolean hstsEnabled,
            @Value("${spring.datasource.password:}") String databasePassword,
            @Value("${app.initial-admin.password:}") String initialAdminPassword
    ) {
        this.environment = environment;
        this.jwtSecret = jwtSecret;
        this.frontendUrls = frontendUrls;
        this.hstsEnabled = hstsEnabled;
        this.databasePassword = databasePassword;
        this.initialAdminPassword = initialAdminPassword;
    }

    @PostConstruct
    void validate() {
        if (!"production".equalsIgnoreCase(environment) && !"prod".equalsIgnoreCase(environment)) return;
        if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32
                || jwtSecret.toLowerCase().contains("replace-with")
                || jwtSecret.toLowerCase().contains("change-me")) {
            throw new IllegalStateException("Production requires a random JWT_SECRET of at least 32 bytes");
        }
        if (!hstsEnabled) throw new IllegalStateException("Production requires SECURITY_HSTS_ENABLED=true behind HTTPS");
        if (Arrays.stream(frontendUrls.split(",")).map(String::trim).anyMatch(origin ->
                origin.isBlank() || !origin.startsWith("https://") || origin.contains("localhost") || origin.contains("127.0.0.1"))) {
            throw new IllegalStateException("Production FRONTEND_URL must contain explicit HTTPS origins");
        }
        if ("news_platform_dev".equals(databasePassword) || "news_platform".equals(databasePassword)) {
            throw new IllegalStateException("Production requires a non-development database password");
        }
        if (initialAdminPassword != null && initialAdminPassword.equals("ChangeMe123!")) {
            throw new IllegalStateException("Production initial admin bootstrap password must be replaced or omitted");
        }
    }
}
