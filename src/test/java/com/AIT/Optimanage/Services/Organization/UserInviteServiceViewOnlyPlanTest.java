package com.AIT.Optimanage.Services.Organization;

import com.AIT.Optimanage.Controllers.dto.UserInviteRequest;
import com.AIT.Optimanage.Exceptions.PlanoSomenteVisualizacaoException;
import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Models.Organization.UserInvite;
import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Repositories.Organization.UserInviteRepository;
import com.AIT.Optimanage.Repositories.UserRepository;
import com.AIT.Optimanage.Services.PlanoAccessGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserInviteServiceViewOnlyPlanTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserInviteRepository inviteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlanoAccessGuard planoAccessGuard;

    private UserInviteService userInviteService;

    @BeforeEach
    void setUp() {
        userInviteService = new UserInviteService(
                organizationRepository,
                inviteRepository,
                userRepository,
                planoAccessGuard
        );
    }

    @Test
    void gerarConviteLancaQuandoPlanoSomenteVisualizacao() {
        Integer organizationId = 55;
        Organization organization = new Organization();
        organization.setTenantId(organizationId);
        User creator = new User();
        creator.setId(9);
        creator.setOrganization(organization);
        creator.setRole(Role.ADMIN);
        creator.setTenantId(organizationId);

        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));

        UserInviteRequest request = UserInviteRequest.builder()
                .role(Role.OPERADOR)
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .email("invitee@example.com")
                .build();

        doThrow(new PlanoSomenteVisualizacaoException()).when(planoAccessGuard)
                .garantirPermissaoDeEscrita(organizationId);

        assertThatThrownBy(() -> userInviteService.gerarConvite(organizationId, request, creator.getId()))
                .isInstanceOf(PlanoSomenteVisualizacaoException.class);

        verify(inviteRepository, never()).save(any(UserInvite.class));
    }

    @Test
    void revogarConviteLancaQuandoPlanoSomenteVisualizacao() {
        Integer organizationId = 101;
        UserInvite invite = UserInvite.builder()
                .code("code-123")
                .build();
        invite.setTenantId(organizationId);

        when(inviteRepository.findByCode("code-123")).thenReturn(Optional.of(invite));
        doThrow(new PlanoSomenteVisualizacaoException()).when(planoAccessGuard)
                .garantirPermissaoDeEscrita(organizationId);

        assertThatThrownBy(() -> userInviteService.revogarConvite(organizationId, "code-123"))
                .isInstanceOf(PlanoSomenteVisualizacaoException.class);

        verify(inviteRepository, never()).delete(invite);
    }
}
