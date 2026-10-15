package com.personalassistant.common.infrastructure.api;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        Map<String, String> details
) {
    public ApiErrorResponse {
        details = details == null ? Map.of() : Map.copyOf(details);
    }
}