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
        Integer organizationId = resolveOrganizationId(request);
        try {
            if (organizationId != null) {
                TenantContext.setTenantId(organizationId);
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("organizationFilter").setParameter("organizationId", organizationId);
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

    private Integer resolveOrganizationId(HttpServletRequest request) {
        String header = request.getHeader("X-Organization-ID");
        if (header != null) {
            try {
                return Integer.valueOf(header);
            } catch (NumberFormatException ignored) {
            }
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtService.extractOrganizationId(token);
        }
        return null;
    }
}
