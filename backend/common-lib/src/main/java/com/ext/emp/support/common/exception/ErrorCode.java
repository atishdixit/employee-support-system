package com.ext.emp.support.common.exception;

import org.springframework.http.HttpStatus;

/** Application-wide error codes, each mapped to the HTTP status it should produce. */
public enum ErrorCode {

    VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    MODEL_INVOCATION_FAILED(HttpStatus.BAD_GATEWAY),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
