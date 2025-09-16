package com.AIT.Optimanage.Repositories;

import com.AIT.Optimanage.Models.Inventory.InventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Integer> {
}
