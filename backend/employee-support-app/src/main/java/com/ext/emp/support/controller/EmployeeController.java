package com.ext.emp.support.controller;

import com.ext.emp.support.model.Employee;
import com.ext.emp.support.service.EmployeeDirectoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employees", description = "The 5 seeded, in-memory employee records")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeDirectoryService employeeDirectoryService;

    public EmployeeController(EmployeeDirectoryService employeeDirectoryService) {
        this.employeeDirectoryService = employeeDirectoryService;
    }

    @GetMapping
    @Operation(summary = "List all seeded employees")
    public List<Employee> listEmployees() {
        return employeeDirectoryService.findAll();
    }

    @GetMapping("/me")
    @Operation(summary = "Look up the currently authenticated employee (from the JWT)")
    public Employee me(Authentication authentication) {
        return employeeDirectoryService.findById(authentication.getName());
    }
}
