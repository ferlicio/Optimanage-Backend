package com.AIT.Optimanage.Models.Audit;

import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Venda.Compatibilidade.ContextoCompatibilidade;
import com.AIT.Optimanage.Support.TenantContext;
import com.AIT.Optimanage.Security.CurrentUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@ActiveProfiles("test")
class OwnerEntityListenerTest {

    @Autowired
    private TestEntityManager entityManager;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        CurrentUser.clear();
        TenantContext.clear();
    }

    @Test
    void prePersistSetsOrganizationWhenNull() {
        TenantContext.setTenantId(1);
        User user = new User();
        user.setNome("John");
        user.setSobrenome("Doe");
        user.setEmail("john@example.com");
        user.setSenha("password");
        user.setAtivo(true);
        user.setRole(Role.OPERADOR);
        user.setOrganizationId(1);
        CurrentUser.set(user);

        ContextoCompatibilidade ctx = new ContextoCompatibilidade();
        ctx.setNome("Example");

        ctx = entityManager.persistAndFlush(ctx);

        assertNotNull(ctx.getOrganizationId());
        assertEquals(1, ctx.getOrganizationId());
    }
}
