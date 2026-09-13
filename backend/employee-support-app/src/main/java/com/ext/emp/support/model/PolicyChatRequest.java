package com.ext.emp.support.model;

import jakarta.validation.constraints.NotBlank;

/** The asking employee is no longer taken from the request body - it comes from the verified JWT. */
public record PolicyChatRequest(@NotBlank(message = "question must not be blank") String question) {
}
