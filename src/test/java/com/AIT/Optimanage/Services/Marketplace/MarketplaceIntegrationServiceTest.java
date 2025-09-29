package com.AIT.Optimanage.Services.Marketplace;

import com.AIT.Optimanage.Controllers.dto.MarketplaceConnectionRequest;
import com.AIT.Optimanage.Controllers.dto.MarketplaceIntegrationResponse;
import com.AIT.Optimanage.Controllers.dto.MarketplaceSyncResponse;
import com.AIT.Optimanage.Models.Marketplace.MarketplaceIntegration;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.Role;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Marketplace.MarketplaceIntegrationRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.PlanoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketplaceIntegrationServiceTest {

    @Mock
    private MarketplaceIntegrationRepository integrationRepository;

    @Mock
    private PlanoService planoService;

    private MarketplaceIntegrationService integrationService;

    private Clock fixedClock;
    private User loggedUser;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2024-02-20T12:00:00Z"), ZoneOffset.UTC);
        integrationService = new MarketplaceIntegrationService(integrationRepository, planoService, fixedClock);

        loggedUser = new User();
        loggedUser.setId(10);
        loggedUser.setTenantId(321);
        loggedUser.setRole(Role.ADMIN);
        CurrentUser.set(loggedUser);
    }

    @AfterEach
    void tearDown() {
        CurrentUser.clear();
    }

    @Test
    void conectarLancaErroQuandoPlanoNaoPermiteIntegracao() {
        Plano plano = Plano.builder().integracaoMarketplaceHabilitada(false).build();
        when(planoService.obterPlanoUsuario(loggedUser)).thenReturn(Optional.of(plano));

        MarketplaceConnectionRequest request = MarketplaceConnectionRequest.builder()
                .marketplace("Mercado Livre")
                .externalAccountId("seller-123")
                .accessToken("token-abc")
                .build();

        assertThatThrownBy(() -> integrationService.conectar(request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Integração com marketplace não habilitada");
    }

    @Test
    void conectarPersisteConfiguracaoQuandoPlanoPermite() {
        Plano plano = Plano.builder().integracaoMarketplaceHabilitada(true).build();
        when(planoService.obterPlanoUsuario(loggedUser)).thenReturn(Optional.of(plano));
        when(integrationRepository.findByOrganizationId(321)).thenReturn(Optional.empty());
        when(integrationRepository.save(any(MarketplaceIntegration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MarketplaceConnectionRequest request = MarketplaceConnectionRequest.builder()
                .marketplace("Mercado Livre")
                .externalAccountId("seller-123")
                .accessToken("token-abc")
                .build();

        MarketplaceIntegrationResponse response = integrationService.conectar(request);

        ArgumentCaptor<MarketplaceIntegration> captor = ArgumentCaptor.forClass(MarketplaceIntegration.class);
        verify(integrationRepository).save(captor.capture());
        MarketplaceIntegration salvo = captor.getValue();

        assertThat(salvo.getOrganizationId()).isEqualTo(321);
        assertThat(salvo.getMarketplace()).isEqualTo("Mercado Livre");
        assertThat(salvo.getAccessToken()).isEqualTo("token-abc");
        assertThat(salvo.getConnectedAt()).isEqualTo(LocalDateTime.ofInstant(fixedClock.instant(), fixedClock.getZone()));

        assertThat(response.getMarketplace()).isEqualTo("Mercado Livre");
        assertThat(response.getExternalAccountId()).isEqualTo("seller-123");
        assertThat(response.getConnectedAt()).isEqualTo(LocalDateTime.ofInstant(fixedClock.instant(), fixedClock.getZone()));
    }

    @Test
    void sincronizarAtualizaDataDeExecucao() {
        Plano plano = Plano.builder().integracaoMarketplaceHabilitada(true).build();
        when(planoService.obterPlanoUsuario(loggedUser)).thenReturn(Optional.of(plano));

        MarketplaceIntegration integration = MarketplaceIntegration.builder()
                .marketplace("B2W")
                .ativo(true)
                .build();
        integration.setTenantId(321);

        when(integrationRepository.findByOrganizationId(321)).thenReturn(Optional.of(integration));
        when(integrationRepository.save(any(MarketplaceIntegration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MarketplaceSyncResponse response = integrationService.sincronizar();

        verify(integrationRepository).findByOrganizationId(321);
        verify(integrationRepository).save(eq(integration));

        LocalDateTime expectedTime = LocalDateTime.ofInstant(fixedClock.instant(), fixedClock.getZone());
        assertThat(integration.getLastSyncAt()).isEqualTo(expectedTime);
        assertThat(response.getMarketplace()).isEqualTo("B2W");
        assertThat(response.getExecutedAt()).isEqualTo(expectedTime);
        assertThat(response.getMensagens()).isNotEmpty();
    }
}
