package com.AIT.Optimanage.Config;

import com.github.bucket4j.Bandwidth;
import com.github.bucket4j.Bucket;
import com.github.bucket4j.Bucket4j;
import com.github.bucket4j.Refill;
import io.micrometer.core.instrument.Counter;
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
 * Filter that limits the number of requests per user or IP using Bucket4j.
 * Blocked requests are recorded using Micrometer metrics for monitoring.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final long CAPACITY = 100;
    private static final Duration DURATION = Duration.ofMinutes(1);
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Counter blockedRequests;

    public RateLimitingFilter(MeterRegistry meterRegistry) {
        this.blockedRequests = Counter.builder("rate_limit.blocked_requests")
                .description("Number of requests blocked due to rate limiting")
                .register(meterRegistry);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String key = resolveKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            blockedRequests.increment();
            response.setStatus(429);
            response.getWriter().write("Too many requests");
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

