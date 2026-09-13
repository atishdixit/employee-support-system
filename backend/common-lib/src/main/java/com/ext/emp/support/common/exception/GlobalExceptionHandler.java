package com.ext.emp.support.common.exception;

import com.ext.emp.support.common.logging.CorrelationIdConstants;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Turns every exception into the same ErrorResponse shape, tagged with the request's correlation id. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex, HttpServletRequest request) {
        HttpStatus status = ex.getErrorCode().getHttpStatus();
        logger.warn("Handled {} -> {}: {}", ex.getErrorCode(), status.value(), ex.getMessage());
        return ResponseEntity.status(status).body(toErrorResponse(status, ex.getMessage(), request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled exception while processing {}", request.getRequestURI(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(toErrorResponse(status, "An unexpected error occurred", request));
    }

    private ErrorResponse toErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        String correlationId = ThreadContext.get(CorrelationIdConstants.MDC_KEY);
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                correlationId);
    }
}
