package com.ext.emp.support.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads (or generates) a correlation id for every request, makes it available to every log
 * line for that request via Log4j2's ThreadContext, and echoes it back as a response header so
 * a caller can correlate their own logs with the server's.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = request.getHeader(CorrelationIdConstants.HEADER_NAME);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        response.setHeader(CorrelationIdConstants.HEADER_NAME, correlationId);
        ThreadContext.put(CorrelationIdConstants.MDC_KEY, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            ThreadContext.remove(CorrelationIdConstants.MDC_KEY);
        }
    }
}
