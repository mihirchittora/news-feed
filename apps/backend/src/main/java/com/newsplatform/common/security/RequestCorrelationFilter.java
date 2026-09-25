package com.newsplatform.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/** Adds a safe request identifier to every response, error body, MDC, and access log. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {
    public static final String REQUEST_ID_ATTRIBUTE = RequestCorrelationFilter.class.getName() + ".requestId";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");
    private static final Logger log = LoggerFactory.getLogger(RequestCorrelationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || !SAFE_REQUEST_ID.matcher(requestId).matches()) {
            requestId = UUID.randomUUID().toString();
        }
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        long started = System.nanoTime();
        try (MDC.MDCCloseable ignored = MDC.putCloseable("requestId", requestId)) {
            filterChain.doFilter(request, response);
        } finally {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userId = authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user
                    ? user.id().toString() : "anonymous";
            log.info("request completed requestId={} method={} path={} status={} durationMs={} userId={}",
                    requestId, request.getMethod(), request.getRequestURI(), response.getStatus(),
                    (System.nanoTime() - started) / 1_000_000, userId);
            MDC.remove("requestId");
        }
    }
}
