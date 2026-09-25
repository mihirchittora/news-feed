package com.newsplatform.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsplatform.common.error.SecurityErrorWriter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

class AuthAbuseProtectionFilterTest {
    @Test
    void limitsAuthenticationAttemptsPerClientAndEndpoint() throws Exception {
        AuthAbuseProtectionFilter filter = new AuthAbuseProtectionFilter(new SecurityErrorWriter(new ObjectMapper().findAndRegisterModules()), 2, 60);
        for (int attempt = 0; attempt < 2; attempt++) {
            MockHttpServletRequest request = request("/api/v1/auth/login");
            filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        }
        MockHttpServletResponse blocked = new MockHttpServletResponse();
        filter.doFilter(request("/api/v1/auth/login"), blocked, new MockFilterChain());

        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getHeader("Retry-After")).isNotBlank();
        assertThat(blocked.getContentAsString()).contains("RATE_LIMITED");
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr("192.0.2.10");
        request.setAttribute(RequestCorrelationFilter.REQUEST_ID_ATTRIBUTE, "test-request");
        return request;
    }
}
