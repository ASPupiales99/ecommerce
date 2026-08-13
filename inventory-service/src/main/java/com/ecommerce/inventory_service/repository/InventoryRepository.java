package com.ecommerce.inventory_service.repository;

import com.ecommerce.inventory_service.model.Inventory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
  Optional<Inventory> findBySku(String sku);

  boolean existsBySku(String sku);
}
