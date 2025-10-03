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
import com.AIT.Optimanage.Services.AuditTrailService;
import com.AIT.Optimanage.Support.PlatformConstants;
import com.AIT.Optimanage.Support.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
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
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private com.AIT.Optimanage.Config.JwtService jwtService;

    @Mock
    private AuditTrailService auditTrailService;

    @InjectMocks
    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void criarOrganizacaoPersistsWithGeneratedTenant() {
        Plano plano = Plano.builder()
                .nome("Trial")
                .valor(0f)
                .duracaoDias(30)
                .qtdAcessos(1)
                .maxUsuarios(10)
                .build();
        plano.setId(12);
        when(planoRepository.findById(1)).thenReturn(Optional.of(plano));
        when(passwordEncoder.encode("ownerPass")).thenReturn("encodedPass");

        ArgumentCaptor<User> ownerCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedOwner = invocation.getArgument(0);
            savedOwner.setId(5);
            return savedOwner;
        });

        ArgumentCaptor<Organization> organizationCaptor = ArgumentCaptor.forClass(Organization.class);
        when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> {
            Organization savedOrg = invocation.getArgument(0);
            savedOrg.setId(42);
            return savedOrg;
        });

        doNothing().when(userRepository).updateOrganizationTenant(eq(5), eq(42));
        doNothing().when(organizationRepository).updateOrganizationTenant(eq(42));

        OrganizationRequest request = OrganizationRequest.builder()
                .planoId(1)
                .cnpj("12345678901234")
                .razaoSocial("Empresa Exemplo")
                .nomeFantasia("Exemplo")
                .telefone("11999999999")
                .email("contato@example.com")
                .permiteOrcamento(true)
                .dataAssinatura(LocalDate.now())
                .owner(UserRequest.builder()
                        .nome("Owner")
                        .sobrenome("Example")
                        .email("owner@example.com")
                        .senha("ownerPass")
                        .role(Role.OWNER)
                        .build())
                .build();

        User creator = new User();
        creator.setRole(Role.ADMIN);
        creator.setTenantId(PlatformConstants.PLATFORM_ORGANIZATION_ID);

        TenantContext.setTenantId(99);

        organizationService.criarOrganizacao(request, creator);

        verify(userRepository).save(ownerCaptor.capture());
        verify(organizationRepository).save(organizationCaptor.capture());
        verify(userRepository).updateOrganizationTenant(5, 42);
        verify(organizationRepository).updateOrganizationTenant(42);

        User persistedOwner = ownerCaptor.getValue();
        Organization persistedOrganization = organizationCaptor.getValue();

        assertThat(persistedOwner.getOrganizationId()).isEqualTo(42);
        assertThat(persistedOrganization.getOrganizationId()).isEqualTo(42);
        assertThat(TenantContext.getTenantId()).isEqualTo(99);

        verify(auditTrailService).recordPlanSubscription(
                eq(persistedOrganization),
                isNull(),
                eq(plano),
                eq(false),
                eq(true)
        );
    }
}
