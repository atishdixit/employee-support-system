package com.ext.emp.support.service;

import com.ext.emp.support.exception.UnknownEmployeeException;
import com.ext.emp.support.model.Employee;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * In-memory directory of 5 seeded employees — deliberately no database, per the project's
 * scope ("only a small prompt, no RAG"). Backs both GET /api/v1/employees (for the UI's
 * "acting as" selector) and the per-request employee-context lookup used to personalize
 * prompt stuffing in PolicyAssistantService.
 */
@Service
public class EmployeeDirectoryService {

    private final Map<String, Employee> employeesById;

    public EmployeeDirectoryService() {
        List<Employee> seed = List.of(
                new Employee("E001", "Asha Verma", "Engineering", "Senior Software Engineer", LocalDate.of(2021, 6, 14)),
                new Employee("E002", "Rohan Mehta", "Sales", "Account Executive", LocalDate.of(2023, 1, 9)),
                new Employee("E003", "Priya Nair", "Human Resources", "HR Business Partner", LocalDate.of(2019, 3, 2)),
                new Employee("E004", "Karan Singh", "Engineering", "Engineering Manager", LocalDate.of(2018, 11, 20)),
                new Employee("E005", "Meera Iyer", "Finance", "Financial Analyst", LocalDate.of(2022, 8, 30)));
        this.employeesById = seed.stream().collect(Collectors.toMap(Employee::id, Function.identity()));
    }

    public List<Employee> findAll() {
        return List.copyOf(employeesById.values());
    }

    public Employee findById(String employeeId) {
        Employee employee = employeesById.get(employeeId);
        if (employee == null) {
            throw new UnknownEmployeeException(employeeId);
        }
        return employee;
    }
}
