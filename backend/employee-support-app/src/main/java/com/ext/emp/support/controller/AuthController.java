package com.ext.emp.support.controller;

import com.ext.emp.support.common.security.JwtTokenProvider;
import com.ext.emp.support.exception.InvalidCredentialsException;
import com.ext.emp.support.model.Employee;
import com.ext.emp.support.model.LoginRequest;
import com.ext.emp.support.model.LoginResponse;
import com.ext.emp.support.security.DemoUserService;
import com.ext.emp.support.service.EmployeeDirectoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Demo login - see docs/SETUP.md for the 5 demo accounts")
public class AuthController {

    private final DemoUserService demoUserService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmployeeDirectoryService employeeDirectoryService;

    public AuthController(
            DemoUserService demoUserService,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            EmployeeDirectoryService employeeDirectoryService) {
        this.demoUserService = demoUserService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.employeeDirectoryService = employeeDirectoryService;
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with one of the 5 demo accounts and receive a JWT")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        DemoUserService.DemoUser user = demoUserService.findByUsername(request.username())
                .filter(u -> passwordEncoder.matches(request.password(), u.passwordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        Employee employee = employeeDirectoryService.findById(user.employeeId());
        String token = jwtTokenProvider.generateToken(employee.id(), Map.of("name", employee.name()));

        return new LoginResponse(token, jwtTokenProvider.tokenTtlSeconds(), employee);
    }
}
