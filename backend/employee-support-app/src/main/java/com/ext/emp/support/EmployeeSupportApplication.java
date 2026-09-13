package com.ext.emp.support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ext.emp.support")
public class EmployeeSupportApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeSupportApplication.class, args);
    }
}
