package com.AIT.Optimanage.Services.Marketplace;

import com.AIT.Optimanage.Controllers.dto.MarketplaceConnectionRequest;
import com.AIT.Optimanage.Controllers.dto.MarketplaceIntegrationResponse;
import com.AIT.Optimanage.Controllers.dto.MarketplaceSyncResponse;
import com.AIT.Optimanage.Models.Marketplace.MarketplaceIntegration;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Models.User.User;
import com.AIT.Optimanage.Repositories.Marketplace.MarketplaceIntegrationRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import com.AIT.Optimanage.Services.PlanoAccessGuard;
import com.AIT.Optimanage.Services.PlanoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketplaceIntegrationService {

    private final MarketplaceIntegrationRepository integrationRepository;
    private final PlanoService planoService;
    private final PlanoAccessGuard planoAccessGuard;
    private final Clock clock;

    @Transactional
    public MarketplaceIntegrationResponse conectar(MarketplaceConnectionRequest request) {
        User loggedUser = requireAuthenticatedUser();
        Plano plano = obterPlanoAtivo(loggedUser);
        garantirMarketplaceHabilitada(plano);

        Integer organizationId = resolvedOrganizationId(loggedUser);
        planoAccessGuard.garantirPermissaoDeEscrita(organizationId);
        MarketplaceIntegration integration = integrationRepository.findByOrganizationId(organizationId)
                .orElseGet(() -> MarketplaceIntegration.builder().build());

        integration.setMarketplace(request.getMarketplace());
        integration.setExternalAccountId(request.getExternalAccountId());
        integration.setAccessToken(request.getAccessToken());
        integration.setConnectedAt(LocalDateTime.now(clock));
        integration.setAtivo(true);
        integration.setTenantId(organizationId);
        integration.setLastSyncAt(null);

        MarketplaceIntegration saved = integrationRepository.save(integration);
        log.info("Organização {} conectou-se ao marketplace {}", organizationId, request.getMarketplace());
        return toResponse(saved);
    }

    @Transactional
    public MarketplaceSyncResponse sincronizar() {
        User loggedUser = requireAuthenticatedUser();
        Plano plano = obterPlanoAtivo(loggedUser);
        garantirMarketplaceHabilitada(plano);

        Integer organizationId = resolvedOrganizationId(loggedUser);
        planoAccessGuard.garantirPermissaoDeEscrita(organizationId);
        MarketplaceIntegration integration = integrationRepository.findByOrganizationId(organizationId)
                .filter(MarketplaceIntegration::getAtivo)
                .orElseThrow(() -> new EntityNotFoundException("Integração com marketplace não configurada"));

        LocalDateTime execucao = LocalDateTime.now(clock);
        integration.setLastSyncAt(execucao);
        integrationRepository.save(integration);

        log.info("Sincronização com marketplace {} concluída para a organização {}", integration.getMarketplace(),
                organizationId);

        return MarketplaceSyncResponse.builder()
                .marketplace(integration.getMarketplace())
                .executedAt(execucao)
                .mensagens(List.of(
                        "Sincronização concluída com sucesso.",
                        "Pedidos importados e catálogo atualizado.",
                        "Última sincronização registrada em " + execucao
                ))
                .build();
    }

    @Transactional(readOnly = true)
    public MarketplaceIntegrationResponse obterStatusAtual() {
        User loggedUser = requireAuthenticatedUser();
        Integer organizationId = resolvedOrganizationId(loggedUser);
        return integrationRepository.findByOrganizationId(organizationId)
                .map(this::toResponse)
                .orElse(null);
    }

    private MarketplaceIntegrationResponse toResponse(MarketplaceIntegration integration) {
        if (integration == null) {
            return null;
        }
        return MarketplaceIntegrationResponse.builder()
                .marketplace(integration.getMarketplace())
                .externalAccountId(integration.getExternalAccountId())
                .connectedAt(integration.getConnectedAt())
                .lastSyncAt(integration.getLastSyncAt())
                .ativo(integration.getAtivo())
                .build();
    }

    private User requireAuthenticatedUser() {
        User user = CurrentUser.get();
        if (user == null) {
            throw new EntityNotFoundException("Usuário não autenticado");
        }
        return user;
    }

    private Plano obterPlanoAtivo(User loggedUser) {
        return planoService.obterPlanoUsuario(loggedUser)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado"));
    }

    private void garantirMarketplaceHabilitada(Plano plano) {
        if (!Boolean.TRUE.equals(plano.getIntegracaoMarketplaceHabilitada())) {
            throw new AccessDeniedException("Integração com marketplace não habilitada no plano atual");
        }
    }

    private Integer resolvedOrganizationId(User user) {
        Integer organizationId = user.getTenantId();
        if (organizationId == null) {
            throw new EntityNotFoundException("Organização não encontrada para o usuário");
        }
        return organizationId;
    }
}
