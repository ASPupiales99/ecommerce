package com.ecommerce.order_service.repository;

import com.ecommerce.order_service.model.OutboxEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
  List<OutboxEvent> findByProcessedFalse();
}
