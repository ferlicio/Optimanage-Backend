package com.AIT.Optimanage.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that generates a correlation id for each request and stores it in the MDC.
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final String CORRELATION_ID_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID_KEY, correlationId);
        try {
            filterChain.doFilter(request, response);
            log.info("Request {} {} completed successfully - correlationId: {}", request.getMethod(), request.getRequestURI(), correlationId);
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }
}
