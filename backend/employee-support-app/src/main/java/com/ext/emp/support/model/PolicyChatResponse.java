package com.ext.emp.support.model;

/** Structured answer returned to the caller — Bean-based Response Structure. */
public record PolicyChatResponse(
        String answer,
        PolicyCategory category,
        String employeeContext,
        String correlationId,
        TokenUsage tokenUsage) {

    /** Token accounting for the underlying model call, or null for a guardrail short-circuit. */
    public record TokenUsage(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
    }
}
