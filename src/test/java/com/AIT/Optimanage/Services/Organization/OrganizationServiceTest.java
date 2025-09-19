package com.AIT.Optimanage.Services.Organization;

import com.AIT.Optimanage.Controllers.User.dto.UserRequest;
import com.AIT.Optimanage.Controllers.dto.OrganizationRequest;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Repositories.PlanoRepository;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Services.Organization.OrganizationService;
import com.AIT.Optimanage.Support.PlatformConstants;
import com.AIT.Optimanage.Support.TenantContext;
import com.AIT.Optimanage.Config.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
    private JwtService jwtService;

    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        organizationService = new OrganizationService(
                organizationRepository,
                userRepository,
                planoRepository,
                new BCryptPasswordEncoder(),
                jwtService
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void criarOrganizacaoDefineTenantFinalParaOwnerEOrganization() {
        Plano plano = Plano.builder()
                .nome("Premium")
                .valor(100.0f)
                .duracaoDias(30)
                .qtdAcessos(10)
                .maxUsuarios(5)
                .build();
        plano.setId(2);
        plano.setTenantId(PlatformConstants.PLATFORM_ORGANIZATION_ID);

        when(planoRepository.findById(2)).thenReturn(Optional.of(plano));

        AtomicReference<User> savedOwnerRef = new AtomicReference<>();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10);
            savedOwnerRef.set(user);
            return user;
        });

        AtomicReference<Organization> savedOrganizationRef = new AtomicReference<>();
        when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> {
            Organization organization = invocation.getArgument(0);
            organization.setId(50);
            savedOrganizationRef.set(organization);
            return organization;
        });

        OrganizationRequest request = OrganizationRequest.builder()
                .cnpj("12345678901234")
                .razaoSocial("Empresa Exemplo")
                .nomeFantasia("Exemplo")
                .telefone("11999999999")
                .email("contato@exemplo.com")
                .permiteOrcamento(true)
                .dataAssinatura(LocalDate.now())
                .planoId(2)
                .owner(UserRequest.builder()
                        .nome("Owner")
                        .sobrenome("Example")
                        .email("owner@example.com")
                        .senha("senha123")
                        .role(Role.OWNER)
                        .build())
                .build();

        User creator = User.builder()
                .role(Role.ADMIN)
                .ativo(true)
                .build();
        creator.setTenantId(PlatformConstants.PLATFORM_ORGANIZATION_ID);

        TenantContext.setTenantId(999);

        organizationService.criarOrganizacao(request, creator);

        verify(userRepository).updateOrganizationId(10, 50);
        verify(organizationRepository).updateOrganizationId(50);

        assertThat(savedOwnerRef.get()).isNotNull();
        assertThat(savedOwnerRef.get().getOrganizationId()).isEqualTo(50);

        assertThat(savedOrganizationRef.get()).isNotNull();
        assertThat(savedOrganizationRef.get().getOrganizationId()).isEqualTo(50);

        assertThat(TenantContext.getTenantId()).isEqualTo(999);
    }
}
