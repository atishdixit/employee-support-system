package com.ext.emp.support.common.security;

import java.util.Collection;
import java.util.Map;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * A generic, already-authenticated token built from a validated JWT's subject + claims.
 * Carries no domain concept (no "employeeId" field) so it can be reused by any service that
 * adopts JwtTokenProvider - the owning application reads whatever claims it needs off
 * {@link #getClaims()}.
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String subject;
    private final Map<String, Object> claims;

    public JwtAuthenticationToken(String subject, Map<String, Object> claims, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.subject = subject;
        this.claims = claims;
        setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        return subject;
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public String getName() {
        return subject;
    }

    public Map<String, Object> getClaims() {
        return claims;
    }
}
