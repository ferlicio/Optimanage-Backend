package com.AIT.Optimanage.Config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Filter that limits requests hitting authentication endpoints.
 * Requests are tracked per user (if authenticated) or per remote IP address.
 * Metrics are recorded through Micrometer so limits can be monitored via the
 * Actuator metrics endpoint.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    /** Maximum number of auth requests allowed during the window. */
    private static final long CAPACITY = 5;
    /** Length of the rate‑limit window. */
    private static final Duration DURATION = Duration.ofMinutes(1);

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;

    public RateLimitingFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        if (!path.startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = resolveKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());
        if (bucket.tryConsume(1)) {
            meterRegistry.counter("rate_limit.auth.allowed").increment();
            filterChain.doFilter(request, response);
        } else {
            meterRegistry.counter("rate_limit.auth.blocked").increment();
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Você excedeu o limite de tentativas. Tente novamente mais tarde.\"}");
        }
    }

    private Bucket newBucket() {
        Refill refill = Refill.greedy(CAPACITY, DURATION);
        Bandwidth limit = Bandwidth.classic(CAPACITY, refill);
        return Bucket4j.builder().addLimit(limit).build();
    }

    private String resolveKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return request.getRemoteAddr();
    }
}

