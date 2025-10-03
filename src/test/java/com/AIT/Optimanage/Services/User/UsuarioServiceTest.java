package com.AIT.Optimanage.Services.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.AIT.Optimanage.Controllers.User.dto.UserResponse;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Repositories.PlanoRepository;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Support.PlatformConstants;
import com.AIT.Optimanage.Services.AuditTrailService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private PlanoRepository planoRepository;
    @Mock private CacheManager cacheManager;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private AuditTrailService auditTrailService;

    @InjectMocks private UsuarioService usuarioService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().build();
        currentUser.setId(1);
        currentUser.setTenantId(10);
        CurrentUser.set(currentUser);
    }

    @AfterEach
    void tearDown() {
        CurrentUser.clear();
    }

    @Test
    void atualizarPlanoAtivoRegistraAuditoria() {
        User usuario = User.builder().role(Role.OWNER).build();
        usuario.setId(1);
        usuario.setTenantId(10);
        usuario.setOrganizationId(77);

        Plano planoAnterior = Plano.builder()
                .nome("Trial")
                .valor(0f)
                .duracaoDias(30)
                .qtdAcessos(1)
                .build();
        planoAnterior.setId(5);

        Plano novoPlano = Plano.builder()
                .nome("Premium")
                .valor(199.9f)
                .duracaoDias(30)
                .qtdAcessos(1)
                .build();
        novoPlano.setId(8);

        Organization organization = Organization.builder()
                .ownerUser(usuario)
                .planoAtivoId(planoAnterior)
                .build();
        organization.setId(77);
        organization.setTenantId(77);

        when(userRepository.findByIdAndOrganizationId(1, 10)).thenReturn(Optional.of(usuario));
        when(organizationRepository.findById(77)).thenReturn(Optional.of(organization));
        when(planoRepository.findById(5)).thenReturn(Optional.of(planoAnterior));
        when(planoRepository.findById(8)).thenReturn(Optional.of(novoPlano));
        when(userRepository.countByOrganizationIdAndAtivoTrue(77)).thenReturn(1L);
        when(organizationRepository.save(organization)).thenReturn(organization);

        usuarioService.atualizarPlanoAtivo(1, 8);

        verify(auditTrailService).recordPlanSubscription(
                eq(organization),
                eq(planoAnterior),
                eq(novoPlano),
                eq(true),
                eq(false)
        );
    }

    @Test
    void listarUsuariosRestringeAoTenantDoUsuarioAtual() {
        Pageable pageable = PageRequest.of(0, 5);
        User usuario = User.builder().nome("Maria").build();
        usuario.setId(2);
        usuario.setTenantId(10);
        Page<User> usuarios = new PageImpl<>(List.of(usuario));

        when(userRepository.findAllByOrganizationId(eq(10), eq(pageable))).thenReturn(usuarios);

        Page<UserResponse> resposta = usuarioService.listarUsuarios(pageable);

        assertThat(resposta.getContent()).hasSize(1);
        verify(userRepository).findAllByOrganizationId(10, pageable);
        verify(userRepository, never()).findAll(pageable);
    }

    @Test
    void buscarUsuarioDeOutroTenantLancaAccessDenied() {
        when(userRepository.findByIdAndOrganizationId(99, 10)).thenReturn(Optional.empty());
        when(userRepository.existsById(99)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.buscarUsuario(99))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("organização atual");

        verify(userRepository).findByIdAndOrganizationId(99, 10);
        verify(userRepository).existsById(99);
    }

    @Test
    void organizacaoPlataformaMantemAcessoGlobal() {
        currentUser.setTenantId(PlatformConstants.PLATFORM_ORGANIZATION_ID);
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> usuarios = new PageImpl<>(List.of());

        when(userRepository.findAll(pageable)).thenReturn(usuarios);

        Page<UserResponse> resposta = usuarioService.listarUsuarios(pageable);

        assertThat(resposta.getContent()).isEmpty();
        verify(userRepository).findAll(pageable);
        verify(userRepository, never()).findAllByOrganizationId(anyInt(), any(Pageable.class));
    }
}
