package com.ecommerce.order_service.repository;

import com.ecommerce.order_service.model.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

  List<Order> findByUserId(String userId);

  Optional<Order> findByOrderNumber(String orderNumber);
}
