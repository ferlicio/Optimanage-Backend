package com.AIT.Optimanage.Services;

import com.AIT.Optimanage.Models.Audit.AuditTrail;
import com.AIT.Optimanage.Repositories.Audit.AuditTrailRepository;
import com.AIT.Optimanage.Security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditTrailService {

    private final AuditTrailRepository auditTrailRepository;

    public void recordPlanQuotaChange(Integer planId, String details) {
        record("PLANO", planId, "ALTERACAO_COTAS", details);
    }

    public void recordDiscountRuleChange(Integer entityId, String details) {
        record("DESCONTO", entityId, "ALTERACAO_REGRAS_DESCONTO", details);
    }

    public void recordSaleCancellation(Integer vendaId, String details) {
        record("VENDA", vendaId, "CANCELAMENTO_VENDA", details);
    }

    public void record(String entityType, Integer entityId, String action, String details) {
        if (entityId == null) {
            return;
        }
        AuditTrail entry = AuditTrail.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .details(details)
                .build();

        Integer organizationId = CurrentUser.getOrganizationId();
        if (organizationId != null) {
            entry.setTenantId(organizationId);
        }

        auditTrailRepository.save(entry);
    }
}
