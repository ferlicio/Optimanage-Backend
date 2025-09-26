package com.AIT.Optimanage.Repositories.CashFlow;

import com.AIT.Optimanage.Models.CashFlow.CashFlowEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CashFlowEntryRepository extends JpaRepository<CashFlowEntry, Integer>, JpaSpecificationExecutor<CashFlowEntry> {
    Optional<CashFlowEntry> findByIdAndOrganizationId(Integer id, Integer organizationId);
}
