package com.ext.emp.support.controller;

import com.ext.emp.support.model.Employee;
import com.ext.emp.support.service.EmployeeDirectoryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeDirectoryService employeeDirectoryService;

    public EmployeeController(EmployeeDirectoryService employeeDirectoryService) {
        this.employeeDirectoryService = employeeDirectoryService;
    }

    @GetMapping
    public List<Employee> listEmployees() {
        return employeeDirectoryService.findAll();
    }
}
