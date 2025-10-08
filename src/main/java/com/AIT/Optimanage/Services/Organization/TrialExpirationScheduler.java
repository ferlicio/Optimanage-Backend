package com.AIT.Optimanage.Services.Organization;

import com.AIT.Optimanage.Models.Organization.Organization;
import com.AIT.Optimanage.Models.Organization.TrialType;
import com.AIT.Optimanage.Models.Plano;
import com.AIT.Optimanage.Repositories.Organization.OrganizationRepository;
import com.AIT.Optimanage.Repositories.PlanoRepository;
import com.AIT.Optimanage.Services.AuditTrailService;
import com.AIT.Optimanage.Support.PlatformConstants;
import com.AIT.Optimanage.Support.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TrialExpirationScheduler {

    private final OrganizationRepository organizationRepository;
    private final PlanoRepository planoRepository;
    private final AuditTrailService auditTrailService;
    private final CacheManager cacheManager;
    private final Clock clock;

    @Scheduled(cron = "${schedule.trial-expiration.cron}")
    @Transactional
    public void downgradeExpiredTrials() {
        Integer previousTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(PlatformConstants.PLATFORM_ORGANIZATION_ID);

            Plano platformBasePlan = planoRepository.findByNomeIgnoreCase(PlatformConstants.VIEW_ONLY_PLAN_NAME)
                    .orElseThrow(() -> new IllegalStateException("Plano base de visualização não encontrado"));

            LocalDate today = LocalDate.now(clock);
            List<Organization> organizations = organizationRepository.findAllByTrialTipoIsNotNull();
            for (Organization organization : organizations) {
                TrialType trialType = organization.getTrialTipo();
                Integer currentPlanId = organization.getPlanoAtivoId();
                Plano currentPlan = currentPlanId != null
                        ? planoRepository.findById(currentPlanId).orElse(null)
                        : null;
                if (!isTrialPlan(trialType, currentPlan)) {
                    continue;
                }

                LocalDate trialEndDate = resolveTrialEndDate(organization, currentPlan, trialType);
                if (trialEndDate == null || trialEndDate.isAfter(today)) {
                    continue;
                }

                Plano tenantViewOnlyPlan = resolveTenantViewOnlyPlan(organization, platformBasePlan);
                boolean planChanged = currentPlan == null || !currentPlan.getId().equals(tenantViewOnlyPlan.getId());
                Plano previousPlan = currentPlan;
                TrialType previousTrialType = trialType;
                organization.setPlanoAtivoId(tenantViewOnlyPlan);
                organization.setTrialInicio(null);
                organization.setTrialFim(null);
                organization.setTrialTipo(null);
                organizationRepository.save(organization);

                evictPlanoCache(organization.getId());
                if (planChanged) {
                    recordAudit(organization, previousPlan, tenantViewOnlyPlan, previousTrialType);
                }
            }
        } finally {
            if (previousTenant != null) {
                TenantContext.setTenantId(previousTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    private Plano resolveTenantViewOnlyPlan(Organization organization, Plano platformBasePlan) {
        Integer previousTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(organization.getId());
            return planoRepository.findByNomeIgnoreCaseAndOrganizationId(
                            PlatformConstants.VIEW_ONLY_PLAN_NAME,
                            organization.getId())
                    .orElseGet(() -> cloneViewOnlyPlanForTenant(platformBasePlan));
        } finally {
            if (previousTenant != null) {
                TenantContext.setTenantId(previousTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    private Plano cloneViewOnlyPlanForTenant(Plano platformBasePlan) {
        Plano tenantPlan = Plano.builder()
                .nome(platformBasePlan.getNome())
                .valor(platformBasePlan.getValor())
                .duracaoDias(platformBasePlan.getDuracaoDias())
                .qtdAcessos(platformBasePlan.getQtdAcessos())
                .maxUsuarios(platformBasePlan.getMaxUsuarios())
                .maxProdutos(platformBasePlan.getMaxProdutos())
                .maxClientes(platformBasePlan.getMaxClientes())
                .maxFornecedores(platformBasePlan.getMaxFornecedores())
                .maxServicos(platformBasePlan.getMaxServicos())
                .agendaHabilitada(platformBasePlan.getAgendaHabilitada())
                .recomendacoesHabilitadas(platformBasePlan.getRecomendacoesHabilitadas())
                .pagamentosHabilitados(platformBasePlan.getPagamentosHabilitados())
                .suportePrioritario(platformBasePlan.getSuportePrioritario())
                .monitoramentoEstoqueHabilitado(platformBasePlan.getMonitoramentoEstoqueHabilitado())
                .metricasProdutoHabilitadas(platformBasePlan.getMetricasProdutoHabilitadas())
                .integracaoMarketplaceHabilitada(platformBasePlan.getIntegracaoMarketplaceHabilitada())
                .build();
        return planoRepository.save(tenantPlan);
    }

    private LocalDate resolveTrialEndDate(Organization organization, Plano currentPlan, TrialType trialType) {
        LocalDate trialEnd = organization.getTrialFim();
        if (trialEnd != null) {
            return trialEnd;
        }

        if (trialType == TrialType.PLAN_DEFAULT && currentPlan != null) {
            Integer duration = currentPlan.getDuracaoDias();
            LocalDate trialStart = organization.getTrialInicio();
            if (trialStart != null && duration != null && duration > 0) {
                return trialStart.plusDays(duration);
            }
        }

        return null;
    }

    private boolean isTrialPlan(TrialType trialType, Plano plan) {
        if (trialType != null) {
            return true;
        }
        if (plan == null) {
            return false;
        }
        Float value = plan.getValor();
        return value == null || Float.compare(value, 0f) <= 0;
    }

    private void evictPlanoCache(Integer organizationId) {
        if (organizationId == null) {
            return;
        }
        Integer previousTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(organizationId);
            Cache cache = cacheManager.getCache("planos");
            if (cache != null) {
                cache.evict(organizationId);
            }
        } finally {
            if (previousTenant != null) {
                TenantContext.setTenantId(previousTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    private void recordAudit(Organization organization, Plano previousPlan, Plano newPlan, TrialType previousTrialType) {
        Integer previousTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(organization.getId());
            auditTrailService.recordPlanSubscription(
                    organization,
                    previousPlan,
                    newPlan,
                    isTrialPlan(previousTrialType, previousPlan),
                    isTrialPlan(null, newPlan)
            );
        } finally {
            if (previousTenant != null) {
                TenantContext.setTenantId(previousTenant);
            } else {
                TenantContext.clear();
            }
        }
    }
}
