package com.ext.emp.support.advisor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

/**
 * Default advisor - logs a truncated view of the outgoing prompt and the call's elapsed time.
 * Correlation id is not logged explicitly here: it is already attached to every log line by
 * Log4j2's ThreadContext (see CorrelationIdFilter in common-lib) via the pattern layout.
 */
public class RequestTraceAdvisor implements CallAdvisor {

    private static final Logger logger = LogManager.getLogger(RequestTraceAdvisor.class);
    private static final int MAX_LOGGED_CHARS = 200;

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        long startedAtMillis = System.currentTimeMillis();
        logger.info("Chat call starting - prompt: \"{}\"", truncate(request.prompt().getContents()));
        try {
            return chain.nextCall(request);
        } finally {
            logger.info("Chat call finished in {} ms", System.currentTimeMillis() - startedAtMillis);
        }
    }

    private String truncate(String text) {
        if (text == null) {
            return "";
        }
        String singleLine = text.replaceAll("\\s+", " ").trim();
        return singleLine.length() <= MAX_LOGGED_CHARS ? singleLine : singleLine.substring(0, MAX_LOGGED_CHARS) + "...";
    }

    @Override
    public String getName() {
        return "RequestTraceAdvisor";
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
