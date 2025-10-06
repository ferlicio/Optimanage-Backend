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

            Plano basePlan = planoRepository.findByNomeIgnoreCase(PlatformConstants.VIEW_ONLY_PLAN_NAME)
                    .orElseThrow(() -> new IllegalStateException("Plano base de visualização não encontrado"));

            LocalDate today = LocalDate.now(clock);
            List<Organization> organizations = organizationRepository.findAllByTrialTipoIsNotNull();
            for (Organization organization : organizations) {
                Integer currentPlanId = organization.getPlanoAtivoId();
                Plano currentPlan = currentPlanId != null
                        ? planoRepository.findById(currentPlanId).orElse(null)
                        : null;
                if (!isTrialPlan(currentPlan)) {
                    continue;
                }

                LocalDate trialEndDate = resolveTrialEndDate(organization, currentPlan);
                if (trialEndDate == null || trialEndDate.isAfter(today)) {
                    continue;
                }

                boolean planChanged = currentPlan == null || !currentPlan.getId().equals(basePlan.getId());
                Plano previousPlan = currentPlan;
                organization.setPlanoAtivoId(basePlan);
                organization.setTrialInicio(null);
                organization.setTrialFim(null);
                organization.setTrialTipo(null);
                organizationRepository.save(organization);

                evictPlanoCache(organization.getId());
                if (planChanged) {
                    recordAudit(organization, previousPlan, basePlan);
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

    private LocalDate resolveTrialEndDate(Organization organization, Plano currentPlan) {
        LocalDate trialEnd = organization.getTrialFim();
        if (trialEnd != null) {
            return trialEnd;
        }

        if (organization.getTrialTipo() == TrialType.PLAN_DEFAULT && currentPlan != null) {
            Integer duration = currentPlan.getDuracaoDias();
            LocalDate trialStart = organization.getTrialInicio();
            if (trialStart != null && duration != null && duration > 0) {
                return trialStart.plusDays(duration);
            }
        }

        return null;
    }

    private boolean isTrialPlan(Plano plan) {
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

    private void recordAudit(Organization organization, Plano previousPlan, Plano newPlan) {
        Integer previousTenant = TenantContext.getTenantId();
        try {
            TenantContext.setTenantId(organization.getId());
            auditTrailService.recordPlanSubscription(
                    organization,
                    previousPlan,
                    newPlan,
                    isTrialPlan(previousPlan),
                    isTrialPlan(newPlan)
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
