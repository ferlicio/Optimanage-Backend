package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Config.CacheConfig;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Repositories.PlanoRepository;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Services.User.UsuarioService;
import com.AIT.Optimanage.Support.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {PlanoService.class, UsuarioService.class, CacheConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PlanoServiceCacheTest {

    @Autowired
    private PlanoService planoService;

    @Autowired
    private UsuarioService usuarioService;

    @MockBean
    private PlanoRepository planoRepository;

    @MockBean
    private OrganizationRepository organizationRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void obterPlanoUsuarioIsCachedPerTenant() {
        User user = new User();
        user.setId(1);
        user.setTenantId(1);
        Plano plano = new Plano();
        plano.setId(10);
        Organization organization = new Organization();
        organization.setPlanoAtivoId(plano);

        when(organizationRepository.findById(1)).thenReturn(Optional.of(organization));
        when(planoRepository.findById(10)).thenReturn(Optional.of(plano));

        TenantContext.setTenantId(1);
        Optional<Plano> first = planoService.obterPlanoUsuario(user);
        Optional<Plano> second = planoService.obterPlanoUsuario(user);

        assertThat(first).contains(plano);
        assertThat(second).contains(plano);
        verify(planoRepository, times(1)).findById(10);
        verify(organizationRepository, times(1)).findById(1);

        TenantContext.setTenantId(2);
        Optional<Plano> third = planoService.obterPlanoUsuario(user);

        assertThat(third).contains(plano);
        verify(planoRepository, times(2)).findById(10);
        verify(organizationRepository, times(2)).findById(1);
    }

    @Test
    void cacheEvictedWhenUserPlanChanges() {
        User user = new User();
        user.setId(1);
        user.setTenantId(1);
        Plano antigo = new Plano();
        antigo.setId(10);
        Plano novo = new Plano();
        novo.setId(20);
        Organization info = new Organization();
        info.setPlanoAtivoId(antigo);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(organizationRepository.findByOwnerUser(user)).thenReturn(Optional.of(info));
        when(organizationRepository.findById(1)).thenReturn(Optional.of(info));
        when(planoRepository.findById(10)).thenReturn(Optional.of(antigo));
        when(planoRepository.findById(20)).thenReturn(Optional.of(novo));

        Optional<Plano> first = planoService.obterPlanoUsuario(user);
        assertThat(first).contains(antigo);
        verify(organizationRepository, times(1)).findById(1);
        verify(planoRepository, times(1)).findById(10);

        usuarioService.atualizarPlanoAtivo(1, 20);

        Optional<Plano> second = planoService.obterPlanoUsuario(user);
        assertThat(second).contains(novo);
        verify(organizationRepository, times(1)).findByOwnerUser(user);
        verify(organizationRepository, times(2)).findById(1);
        verify(planoRepository, times(1)).findById(10);
        verify(planoRepository, times(2)).findById(20);
    }
}

