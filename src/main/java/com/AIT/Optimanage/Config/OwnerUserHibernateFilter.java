package com.AIT.Optimanage.Config;

import com.AIT.Optimanage.Models.User.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Habilita o filtro de proprietário (ownerUser) do Hibernate em cada requisição,
 * garantindo que apenas registros pertencentes ao usuário autenticado sejam retornados.
 */
@Component
public class OwnerUserHibernateFilter extends OncePerRequestFilter {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Session session = entityManager.unwrap(Session.class);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Filter filter = null;
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            filter = session.enableFilter("ownerUserFilter");
            filter.setParameter("ownerUserId", user.getId());
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (filter != null) {
                session.disableFilter("ownerUserFilter");
            }
        }
    }
}

