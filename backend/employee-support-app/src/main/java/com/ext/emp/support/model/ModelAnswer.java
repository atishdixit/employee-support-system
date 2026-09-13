package com.ext.emp.support.model;

/**
 * The structured shape the LLM itself is asked to produce (Bean-based Response Structure).
 * Deliberately narrow: only fields the model can meaningfully know. Request-scoped metadata
 * (correlation id, token usage) is added afterwards by PolicyAssistantService, not by the model.
 */
public record ModelAnswer(String answer, PolicyCategory category) {
}
