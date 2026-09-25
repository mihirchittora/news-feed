package com.newsplatform.common.config;

import com.newsplatform.common.error.SecurityErrorWriter;
import com.newsplatform.common.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            SecurityErrorWriter securityErrorWriter
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> { })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                securityErrorWriter.write(request, response, 401, "UNAUTHORIZED", "Authentication is required"))
                        .accessDeniedHandler((request, response, exception) ->
                                securityErrorWriter.write(request, response, 403, "FORBIDDEN", "You do not have access to this resource"))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/health",
                                "/api/v1/categories",
                                "/api/v1/categories/**",
                                "/api/v1/feed",
                                "/api/v1/feed/**",
                                "/api/v1/breaking-news",
                                "/api/v1/newspapers",
                                "/api/v1/newspapers/*",
                                "/api/v1/newspapers/*/cover",
                                "/api/v1/stories/*/comments",
                                "/api/v1/media/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/api/v1/stories/*/like", "/api/v1/comments/**").authenticated()
                        .requestMatchers("/api/v1/admin/**").authenticated()
                        .requestMatchers("/api/v1/users/me").authenticated()
                        .requestMatchers("/api/v1/newspapers/*/document").authenticated()
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService unusedFormLoginUserDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Password authentication is not enabled");
        };
    }
}
