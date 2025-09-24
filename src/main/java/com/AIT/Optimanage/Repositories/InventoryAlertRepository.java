package com.AIT.Optimanage.Repositories;

import com.AIT.Optimanage.Models.Inventory.InventoryAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryAlertRepository extends JpaRepository<InventoryAlert, Integer> {

    List<InventoryAlert> findByOrganizationIdOrderBySeverityDescDiasRestantesAsc(Integer organizationId);

    @Modifying
    @Query("delete from InventoryAlert alert where alert.organizationId = :organizationId")
    void deleteByOrganizationId(Integer organizationId);
}
