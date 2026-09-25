package com.newsplatform.common.error;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        Map<String, String> errors,
        String path,
        String requestId
) {
}
