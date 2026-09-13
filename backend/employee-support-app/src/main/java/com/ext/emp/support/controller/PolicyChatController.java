package com.ext.emp.support.controller;

import com.ext.emp.support.model.PolicyChatRequest;
import com.ext.emp.support.model.PolicyChatResponse;
import com.ext.emp.support.service.PolicyAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/policy")
@Tag(name = "Policy Chat", description = "Ask company policy questions as the logged-in employee")
@SecurityRequirement(name = "bearerAuth")
public class PolicyChatController {

    private final PolicyAssistantService policyAssistantService;

    public PolicyChatController(PolicyAssistantService policyAssistantService) {
        this.policyAssistantService = policyAssistantService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Ask a policy question - employee identity comes from the JWT, not the request body")
    public PolicyChatResponse chat(
            @Valid @RequestBody PolicyChatRequest request,
            @RequestParam(value = "debug", defaultValue = "false") boolean debug,
            Authentication authentication) {
        String employeeId = authentication.getName();
        return policyAssistantService.ask(employeeId, request.question(), debug);
    }
}
