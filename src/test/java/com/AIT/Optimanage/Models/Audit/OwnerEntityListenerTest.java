package com.AIT.Optimanage.Models.Audit;

import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.ContextoCompatibilidade;
import com.AIT.Optimanage.Support.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@EnableJpaAuditing
class OwnerEntityListenerTest {

    @Autowired
    private TestEntityManager entityManager;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void prePersistSetsOwnerUserWhenNull() {
        TenantContext.setTenantId(1);
        User user = User.builder()
                .nome("John")
                .sobrenome("Doe")
                .email("john@example.com")
                .senha("password")
                .role(Role.OPERADOR)
                .build();
        entityManager.persistAndFlush(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );

        ContextoCompatibilidade ctx = new ContextoCompatibilidade();
        ctx.setNome("Example");

        ctx = entityManager.persistAndFlush(ctx);

        assertNotNull(ctx.getOwnerUser());
        assertEquals(user.getId(), ctx.getOwnerUser().getId());
    }
}
