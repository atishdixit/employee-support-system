package com.ext.emp.support.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

/**
 * Generic HMAC-signed JWT issuing/parsing, with no knowledge of "Employee" or any other
 * domain concept - the owning application decides what goes in the subject/claims. Kept in
 * common-lib so a second service could reuse the exact same token format later.
 */
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final Duration tokenTtl;

    /**
     * @param secret must be at least 32 characters (256 bits) for HS256; the owning
     *               application is responsible for validating that at startup.
     */
    public JwtTokenProvider(String secret, Duration tokenTtl) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tokenTtl = tokenTtl;
    }

    public String generateToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(tokenTtl)))
                .signWith(signingKey)
                .compact();
    }

    public long tokenTtlSeconds() {
        return tokenTtl.toSeconds();
    }

    /** @return the token's claims, or empty if the token is missing, malformed, expired, or has a bad signature. */
    public java.util.Optional<Claims> parseClaims(String token) {
        try {
            return java.util.Optional.of(Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException ex) {
            return java.util.Optional.empty();
        }
    }
}
