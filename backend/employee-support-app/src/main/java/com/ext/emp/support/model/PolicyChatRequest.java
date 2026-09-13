package com.ext.emp.support.model;

import jakarta.validation.constraints.NotBlank;

/** Incoming question from an employee, scoped to a specific employee's context. */
public record PolicyChatRequest(
        @NotBlank(message = "employeeId must not be blank") String employeeId,
        @NotBlank(message = "question must not be blank") String question) {
}
