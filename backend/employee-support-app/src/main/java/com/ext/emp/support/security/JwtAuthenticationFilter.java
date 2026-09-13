package com.ext.emp.support.security;

import com.ext.emp.support.common.security.JwtAuthenticationToken;
import com.ext.emp.support.common.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads "Authorization: Bearer <token>", validates it via JwtTokenProvider, and - if valid -
 * populates the SecurityContext with a JwtAuthenticationToken whose principal is the
 * employeeId (the token's subject). An absent/invalid token simply leaves the context empty;
 * SecurityConfig's authorization rules are what turn that into a 401 for protected endpoints.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            jwtTokenProvider.parseClaims(token).ifPresent(this::authenticate);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(Claims claims) {
        String employeeId = claims.getSubject();
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
        var authentication = new JwtAuthenticationToken(employeeId, claims, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
