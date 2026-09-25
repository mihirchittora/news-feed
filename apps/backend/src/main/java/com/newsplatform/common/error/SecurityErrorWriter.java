package com.newsplatform.common.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import com.newsplatform.common.security.RequestCorrelationFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
public class SecurityErrorWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletRequest request, HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Object requestId = request.getAttribute(RequestCorrelationFilter.REQUEST_ID_ATTRIBUTE);
        objectMapper.writeValue(response.getWriter(), new ApiError(
                Instant.now(), status, code, message, Map.of(), request.getRequestURI(), requestId == null ? null : requestId.toString()
        ));
    }
}
