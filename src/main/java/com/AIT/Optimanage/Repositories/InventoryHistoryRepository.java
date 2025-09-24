package com.AIT.Optimanage.Repositories;

import com.AIT.Optimanage.Models.Inventory.InventoryAction;
import com.AIT.Optimanage.Models.Inventory.InventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Integer> {

    List<InventoryHistory> findByProdutoIdAndOrganizationIdAndActionAndCreatedAtAfterOrderByCreatedAtAsc(
            Integer produtoId,
            Integer organizationId,
            InventoryAction action,
            LocalDateTime createdAt);
}
