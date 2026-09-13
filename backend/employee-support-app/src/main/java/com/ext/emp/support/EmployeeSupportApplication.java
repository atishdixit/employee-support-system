package com.ext.emp.support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// UserDetailsServiceAutoConfiguration is excluded because auth here is pure stateless JWT
// (see SecurityConfig) - there is no UserDetailsService/form login, so Boot's default
// "generated security password" user would otherwise be created and never used.
@SpringBootApplication(scanBasePackages = "com.ext.emp.support", exclude = UserDetailsServiceAutoConfiguration.class)
public class EmployeeSupportApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeSupportApplication.class, args);
    }
}
