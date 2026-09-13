package com.ext.emp.support.advisor;

import com.ext.emp.support.model.ModelAnswer;
import com.ext.emp.support.model.PolicyCategory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

/**
 * Default advisor (safeguard) - the "someone tries to go out of topic" defense. The stuffed
 * system prompt already instructs the model to refuse off-topic questions, but this advisor
 * adds a cheap, deterministic keyword allowlist check *before* the model is ever called: if
 * the user's question doesn't overlap at all with the five supported policy topics, it
 * short-circuits with a canned refusal and never spends a model call on it.
 */
public class TopicGuardrailAdvisor implements CallAdvisor {

    private static final Logger logger = LogManager.getLogger(TopicGuardrailAdvisor.class);

    private static final Set<String> ALLOWED_KEYWORDS = Set.of(
            "leave", "leaves", "vacation", "pto", "sick",
            "holiday", "holidays",
            "maternity", "paternity", "parental", "adoption",
            "promotion", "promote", "appraisal", "rating", "review",
            "hour", "hours", "shift", "timing", "wfh", "remote", "office",
            "policy", "hr", "help", "hi", "hello");

    private static final String REFUSAL_TEXT =
            "I can only help with questions about company Leave, Holiday, Maternity/Paternity, "
                    + "Promotion, and Office Work Hours policies. Could you rephrase your question "
                    + "around one of those topics?";

    private final ObjectMapper objectMapper;

    public TopicGuardrailAdvisor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        UserMessage userMessage = request.prompt().getUserMessage();
        String question = userMessage != null ? userMessage.getText() : "";

        if (isInScope(question)) {
            return chain.nextCall(request);
        }

        logger.warn("Blocked out-of-scope question before calling the model: \"{}\"", question);
        return buildRefusalResponse();
    }

    private boolean isInScope(String question) {
        if (question == null || question.isBlank()) {
            return false;
        }
        String normalized = question.toLowerCase(Locale.ROOT);
        return ALLOWED_KEYWORDS.stream().anyMatch(normalized::contains);
    }

    private ChatClientResponse buildRefusalResponse() {
        try {
            String json = objectMapper.writeValueAsString(new ModelAnswer(REFUSAL_TEXT, PolicyCategory.OUT_OF_SCOPE));
            ChatResponse chatResponse = ChatResponse.builder()
                    .generations(List.of(new Generation(new AssistantMessage(json))))
                    .build();
            return ChatClientResponse.builder().chatResponse(chatResponse).build();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to build guardrail refusal response", ex);
        }
    }

    @Override
    public String getName() {
        return "TopicGuardrailAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
