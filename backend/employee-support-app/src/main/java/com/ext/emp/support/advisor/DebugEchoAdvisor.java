package com.ext.emp.support.advisor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

/**
 * Per-request advisor: attached only when PolicyChatController receives ?debug=true, unlike
 * the always-on defaults in ChatClientConfig (TokenUsageAuditAdvisor, RequestTraceAdvisor,
 * TopicGuardrailAdvisor). Logs the full, untruncated prompt and answer for troubleshooting.
 */
public class DebugEchoAdvisor implements CallAdvisor {

    private static final Logger logger = LogManager.getLogger(DebugEchoAdvisor.class);

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("[debug] full prompt: {}", request.prompt().getContents());
        ChatClientResponse response = chain.nextCall(request);
        if (response.chatResponse() != null) {
            logger.info("[debug] full raw model output: {}", response.chatResponse().getResult().getOutput().getText());
        }
        return response;
    }

    @Override
    public String getName() {
        return "DebugEchoAdvisor";
    }

    @Override
    public int getOrder() {
        return 30;
    }
}
