package com.AIT.Optimanage.Services.Organization;

import com.AIT.Optimanage.Auth.AuthenticationResponse;
import com.AIT.Optimanage.Controllers.User.dto.UserRequest;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Repositories.PlanoRepository;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Support.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlanoRepository planoRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private com.AIT.Optimanage.Config.JwtService jwtService;

    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        organizationService = new OrganizationService(
                organizationRepository,
                userRepository,
                planoRepository,
                passwordEncoder,
                jwtService
        );
    }

    @AfterEach
    void tearDown() {
        CurrentUser.clear();
        TenantContext.clear();
    }

    @Test
    void adicionarUsuarioDeveNegarAcessoParaOutraOrganizacao() {
        User currentUser = criarUsuarioAtual(10);
        CurrentUser.set(currentUser);

        UserRequest request = UserRequest.builder()
                .nome("Novo")
                .sobrenome("Usuário")
                .email("novo@example.com")
                .senha("senha")
                .role(Role.ADMIN)
                .build();

        assertThrows(AccessDeniedException.class,
                () -> organizationService.adicionarUsuario(99, request));

        verifyNoInteractions(organizationRepository, planoRepository, userRepository, passwordEncoder, jwtService);
    }

    @Test
    void adicionarUsuarioDevePermitirQuandoOrganizacaoCorreta() {
        Integer organizationId = 20;
        User currentUser = criarUsuarioAtual(organizationId);
        CurrentUser.set(currentUser);

        Organization organization = new Organization();
        organization.setTenantId(organizationId);
        Plano plano = new Plano();
        plano.setId(5);
        organization.setPlanoAtivoId(plano);

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(planoRepository.findById(plano.getId())).thenReturn(Optional.of(plano));
        when(userRepository.countByOrganizationIdAndAtivoTrue(organizationId)).thenReturn(0L);
        when(passwordEncoder.encode("segredo"))
                .thenReturn("hash-segredo");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(50);
            return saved;
        });
        when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("token-gerado");

        UserRequest request = UserRequest.builder()
                .nome("Novo")
                .sobrenome("Usuário")
                .email("novo@example.com")
                .senha("segredo")
                .role(Role.ADMIN)
                .build();

        AuthenticationResponse response = organizationService.adicionarUsuario(organizationId, request);

        assertNotNull(response);
        assertEquals("token-gerado", response.getToken());
        assertNull(TenantContext.getTenantId());
    }

    private User criarUsuarioAtual(Integer tenantId) {
        User user = User.builder()
                .nome("Atual")
                .sobrenome("Usuário")
                .email("atual@example.com")
                .senha("senha-atual")
                .role(Role.ADMIN)
                .build();
        user.setTenantId(tenantId);
        return user;
    }
}
