package com.AIT.Optimanage.Config;

import com.AIT.Optimanage.Support.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        Integer tenantId = resolveTenantId(request);
        try {
            if (tenantId != null) {
            TenantContext.setTenantId(tenantId);
            if (!Integer.valueOf(1).equals(tenantId)) {
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("organizationFilter").setParameter("organizationId", tenantId);
            }
        }
            filterChain.doFilter(request, response);
        } finally {
            Session session = entityManager.unwrap(Session.class);
            if (session.getEnabledFilter("organizationFilter") != null) {
                session.disableFilter("organizationFilter");
            }
            TenantContext.clear();
        }
    }

    private Integer resolveTenantId(HttpServletRequest request) {
        String header = request.getHeader("X-Tenant-ID");
        if (header != null) {
            try {
                return Integer.valueOf(header);
            } catch (NumberFormatException ignored) {
            }
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtService.extractClaim(token, claims -> claims.get("tenantId", Integer.class));
        }
        return null;
    }
}
