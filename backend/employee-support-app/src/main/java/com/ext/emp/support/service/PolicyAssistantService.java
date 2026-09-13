package com.ext.emp.support.service;

import com.ext.emp.support.advisor.DebugEchoAdvisor;
import com.ext.emp.support.common.logging.CorrelationIdConstants;
import com.ext.emp.support.exception.PolicyAssistantException;
import com.ext.emp.support.model.Employee;
import com.ext.emp.support.model.ModelAnswer;
import com.ext.emp.support.model.PolicyChatResponse;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * Orchestrates one policy question end to end: looks up the asking employee, stuffs the
 * company-policy system prompt with their context (prompt stuffing + roles), invokes the
 * shared ChatClient (whose default advisors handle guardrails, logging and token auditing),
 * and assembles the final structured PolicyChatResponse - filling in correlationId and
 * tokenUsage itself, since those aren't things the model can meaningfully produce.
 */
@Service
public class PolicyAssistantService {

    private static final Logger logger = LogManager.getLogger(PolicyAssistantService.class);

    private final ChatClient chatClient;
    private final EmployeeDirectoryService employeeDirectoryService;
    private final Resource companyPolicySystemPrompt;

    public PolicyAssistantService(
            ChatClient chatClient,
            EmployeeDirectoryService employeeDirectoryService,
            @Value("classpath:/promptTemplates/companyPolicySystemPrompt.st") Resource companyPolicySystemPrompt) {
        this.chatClient = chatClient;
        this.employeeDirectoryService = employeeDirectoryService;
        this.companyPolicySystemPrompt = companyPolicySystemPrompt;
    }

    public PolicyChatResponse ask(String employeeId, String question, boolean debug) {
        Employee employee = employeeDirectoryService.findById(employeeId);
        String correlationId = ThreadContext.get(CorrelationIdConstants.MDC_KEY);

        Map<String, Object> employeeContextParams = Map.of(
                "employeeName", employee.name(),
                "employeeDepartment", employee.department(),
                "employeeDesignation", employee.designation(),
                "employeeJoiningDate", employee.joiningDate().toString());

        try {
            var requestSpec = chatClient
                    .prompt()
                    .system(spec -> spec.text(companyPolicySystemPrompt).params(employeeContextParams))
                    .user(question);

            if (debug) {
                requestSpec = requestSpec.advisors(new DebugEchoAdvisor());
            }

            ResponseEntity<ChatResponse, ModelAnswer> result = requestSpec.call().responseEntity(ModelAnswer.class);
            ModelAnswer modelAnswer = result.entity();
            PolicyChatResponse.TokenUsage tokenUsage = toTokenUsage(result.response());

            String employeeContextSummary = "%s (%s, %s)".formatted(
                    employee.name(), employee.designation(), employee.department());

            return new PolicyChatResponse(
                    modelAnswer.answer(), modelAnswer.category(), employeeContextSummary, correlationId, tokenUsage);
        } catch (RuntimeException ex) {
            logger.error("Ollama call failed for employee {}", employeeId, ex);
            throw new PolicyAssistantException("The policy assistant is temporarily unavailable. Please try again shortly.", ex);
        }
    }

    private PolicyChatResponse.TokenUsage toTokenUsage(ChatResponse chatResponse) {
        if (chatResponse == null || chatResponse.getMetadata() == null || chatResponse.getMetadata().getUsage() == null) {
            return null;
        }
        Usage usage = chatResponse.getMetadata().getUsage();
        return new PolicyChatResponse.TokenUsage(usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
    }
}
