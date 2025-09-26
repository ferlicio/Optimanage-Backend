package com.AIT.Optimanage.Repositories.CashFlow;

import com.AIT.Optimanage.Models.CashFlow.CashFlowEntry;
import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowStatus;
import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class CashFlowFilters {

    private CashFlowFilters() {
    }

    public static Specification<CashFlowEntry> hasOrganization(Integer organizationId) {
        return (root, query, cb) -> cb.equal(root.get("organizationId"), organizationId);
    }

    public static Specification<CashFlowEntry> hasType(CashFlowType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<CashFlowEntry> hasStatus(CashFlowStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<CashFlowEntry> occursOnOrAfter(LocalDate date) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("movementDate"), date);
    }

    public static Specification<CashFlowEntry> occursOnOrBefore(LocalDate date) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("movementDate"), date);
    }
}
