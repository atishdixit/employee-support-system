package com.ext.emp.support.common.exception;

import com.ext.emp.support.common.logging.CorrelationIdConstants;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationFailure(MethodArgumentNotValidException ex, HttpServletRequest request) {
        // Field @NotBlank/@NotNull messages in this codebase already name the field
        // (e.g. "question must not be blank"), so join them as-is rather than double it up.
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(org.springframework.validation.FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        logger.warn("Validation failed for {}: {}", request.getRequestURI(), message);
        return ResponseEntity.badRequest().body(toErrorResponse(HttpStatus.BAD_REQUEST, message, request));
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
