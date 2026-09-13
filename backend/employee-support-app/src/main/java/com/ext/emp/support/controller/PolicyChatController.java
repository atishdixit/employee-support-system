package com.ext.emp.support.controller;

import com.ext.emp.support.model.PolicyChatRequest;
import com.ext.emp.support.model.PolicyChatResponse;
import com.ext.emp.support.service.PolicyAssistantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/policy")
public class PolicyChatController {

    private final PolicyAssistantService policyAssistantService;

    public PolicyChatController(PolicyAssistantService policyAssistantService) {
        this.policyAssistantService = policyAssistantService;
    }

    @PostMapping("/chat")
    public PolicyChatResponse chat(
            @Valid @RequestBody PolicyChatRequest request,
            @RequestParam(value = "debug", defaultValue = "false") boolean debug) {
        return policyAssistantService.ask(request.employeeId(), request.question(), debug);
    }
}
