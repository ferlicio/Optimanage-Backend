package com.AIT.Optimanage.Config;

import com.AIT.Optimanage.Models.User.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OwnerFilter extends OncePerRequestFilter {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            if (authentication != null && authentication.getPrincipal() instanceof User user) {
                Session session = entityManager.unwrap(Session.class);
                session.enableFilter("ownerFilter").setParameter("userId", user.getId());
            }
            filterChain.doFilter(request, response);
        } finally {
            Session session = entityManager.unwrap(Session.class);
            if (session.getEnabledFilter("ownerFilter") != null) {
                session.disableFilter("ownerFilter");
            }
        }
    }
}
