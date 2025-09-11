package com.AIT.Optimanage.Config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Services.PlanoService;
import com.AIT.Optimanage.Support.TenantContext;

/**
 * Filter that limits requests hitting authentication endpoints.
 * Requests are tracked per user (if authenticated) or per remote IP address.
 * Metrics are recorded through Micrometer so limits can be monitored via the
 * Actuator metrics endpoint. Buckets are stored in a Caffeine cache that
 * expires entries after {@link #BUCKET_CACHE_EXPIRY} of inactivity to prevent
 * unbounded memory usage.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    /** Default number of auth requests allowed during the window. */
    private static final long DEFAULT_CAPACITY = 5;
    /** Default length of the rate‑limit window. */
    private static final Duration DEFAULT_DURATION = Duration.ofMinutes(1);
    /**
     * Idle buckets are evicted after this period, keeping the cache size bounded
     * while allowing active clients to retain their rate limits.
     */
    private static final Duration BUCKET_CACHE_EXPIRY = Duration.ofMinutes(10);

    /** Cache holding buckets for each user/IP and tenant combination. */
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(BUCKET_CACHE_EXPIRY)
            .build();
    private final MeterRegistry meterRegistry;
    private final PlanoService planoService;

    public RateLimitingFilter(MeterRegistry meterRegistry, PlanoService planoService) {
        this.meterRegistry = meterRegistry;
        this.planoService = planoService;
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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String key = bucketKey(request, authentication);
        Bucket bucket;
        String planTag;

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User user) {
            Plano plano = planoService.obterPlanoUsuario(user).orElse(null);
            long capacity = plano != null ? plano.getQtdAcessos() : DEFAULT_CAPACITY;
            Duration duration = plano != null ? Duration.ofDays(plano.getDuracaoDias()) : DEFAULT_DURATION;
            planTag = plano != null ? plano.getNome() : "none";
            bucket = buckets.get(key, k -> newBucket(capacity, duration));
        } else {
            planTag = "anonymous";
            bucket = buckets.get(key, k -> newBucket(DEFAULT_CAPACITY, DEFAULT_DURATION));
        }

        if (bucket.tryConsume(1)) {
            meterRegistry.counter("rate_limit.auth.allowed", Tags.of("plan", planTag)).increment();
            filterChain.doFilter(request, response);
        } else {
            meterRegistry.counter("rate_limit.auth.blocked", Tags.of("plan", planTag)).increment();
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Você excedeu o limite de tentativas. Tente novamente mais tarde.\"}");
        }
    }

    private Bucket newBucket(long capacity, Duration duration) {
        Refill refill = Refill.greedy(capacity, duration);
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket4j.builder().addLimit(limit).build();
    }

    private String bucketKey(HttpServletRequest request, Authentication authentication) {
        String prefix = tenantKey() + ":";
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
            return prefix + authentication.getName();
        }
        return prefix + request.getRemoteAddr();
    }

    private String tenantKey() {
        Integer tenantId = TenantContext.getTenantId();
        return tenantId != null ? tenantId.toString() : "noTenant";
    }
}

