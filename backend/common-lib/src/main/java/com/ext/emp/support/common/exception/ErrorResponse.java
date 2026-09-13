package com.ext.emp.support.common.exception;

import java.time.Instant;

/** Consistent error body returned by every endpoint across the application. */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String correlationId) {
}
