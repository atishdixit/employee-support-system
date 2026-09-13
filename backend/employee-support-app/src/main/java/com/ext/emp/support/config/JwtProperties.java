package com.ext.emp.support.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** app.security.jwt.* - sourced from .env (JWT_SECRET / JWT_EXPIRATION_MINUTES). */
@Component
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {

    private String secret;
    private long expirationMinutes = 60;

    @PostConstruct
    void validate() {
        if (secret == null || secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "app.security.jwt.secret must be set and at least 32 characters (256 bits) long for HS256");
        }
        if (expirationMinutes <= 0) {
            throw new IllegalStateException("app.security.jwt.expiration-minutes must be positive");
        }
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(long expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }
}
