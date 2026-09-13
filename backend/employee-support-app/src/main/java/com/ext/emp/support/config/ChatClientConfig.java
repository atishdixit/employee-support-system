package com.ext.emp.support.config;

import com.ext.emp.support.advisor.RequestTraceAdvisor;
import com.ext.emp.support.advisor.TokenUsageAuditAdvisor;
import com.ext.emp.support.advisor.TopicGuardrailAdvisor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the single shared ChatClient bean. Deliberately holds no defaultSystem: the stuffed
 * policy + employee-context system prompt differs per request (see PolicyAssistantService), so
 * only the provider-agnostic ChatOptions and the always-on advisors are configured as defaults
 * here. See ChatModelProperties for where max/min tokens and temperature come from.
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatModelProperties chatModelProperties,
            ObjectMapper objectMapper) {

        ChatOptions options = ChatOptions.builder()
                .model(chatModelProperties.getModel())
                .temperature(chatModelProperties.getTemperature())
                .maxTokens(chatModelProperties.getMaxTokens())
                .build();

        return chatClientBuilder
                .defaultOptions(options)
                .defaultAdvisors(List.of(
                        new TopicGuardrailAdvisor(objectMapper),
                        new TokenUsageAuditAdvisor(),
                        new RequestTraceAdvisor()))
                .build();
    }
}
