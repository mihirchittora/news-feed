package com.newsplatform.common.security;

import com.newsplatform.common.error.SecurityErrorWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Small bounded-memory fixed-window limiter for abuse-prone unauthenticated endpoints. */
@Component
public class AuthAbuseProtectionFilter extends OncePerRequestFilter {
    private final SecurityErrorWriter errorWriter;
    private final int maxRequests;
    private final long windowSeconds;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public AuthAbuseProtectionFilter(SecurityErrorWriter errorWriter,
                                     @Value("${app.security.rate-limit.auth.max-requests:10}") int maxRequests,
                                     @Value("${app.security.rate-limit.auth.window-seconds:60}") long windowSeconds) {
        this.errorWriter = errorWriter;
        this.maxRequests = Math.max(maxRequests, 1);
        this.windowSeconds = Math.max(windowSeconds, 1);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!"POST".equalsIgnoreCase(request.getMethod()) || !isProtectedPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        long window = Instant.now().getEpochSecond() / windowSeconds;
        String key = request.getRemoteAddr() + "|" + request.getRequestURI();
        Window current = windows.compute(key, (ignored, previous) ->
                previous == null || previous.bucket() != window ? new Window(window, 1) : new Window(window, previous.count() + 1));
        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(entry -> entry.getValue().bucket() < window - 1);
        }
        if (current.count() > maxRequests) {
            long retryAfter = Math.max(1, ((window + 1) * windowSeconds) - Instant.now().getEpochSecond());
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            errorWriter.write(request, response, 429, "RATE_LIMITED", "Too many authentication requests. Please try again later.");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isProtectedPath(String path) {
        return "/api/v1/auth/login".equals(path)
                || "/api/v1/auth/register".equals(path)
                || "/api/v1/auth/staff-setup".equals(path);
    }

    private record Window(long bucket, int count) { }
}
