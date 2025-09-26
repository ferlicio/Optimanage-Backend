package com.AIT.Optimanage.Repositories.CashFlow;

import com.AIT.Optimanage.Models.CashFlow.CashFlowEntry;
import com.AIT.Optimanage.Models.CashFlow.Enums.CashFlowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.time.LocalDate;
import java.util.List;

public interface CashFlowEntryRepository extends JpaRepository<CashFlowEntry, Integer>, JpaSpecificationExecutor<CashFlowEntry> {
    Optional<CashFlowEntry> findByIdAndOrganizationId(Integer id, Integer organizationId);

    List<CashFlowEntry> findAllByOrganizationIdAndStatusAndMovementDateGreaterThanEqual(Integer organizationId,
            CashFlowStatus status, LocalDate movementDate);
}
