package com.ext.emp.support.model;

public record LoginResponse(String token, long expiresInSeconds, Employee employee) {
}
