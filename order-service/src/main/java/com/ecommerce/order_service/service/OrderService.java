package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.OrderRequest;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.model.OrderStatus;
import java.util.List;

public interface OrderService {
  OrderResponse findById(Long orderId);

  List<OrderResponse> findAll();

  List<OrderResponse> getOrderHistory(String userId, boolean isAdmin);

  OrderResponse placeOrder(OrderRequest orderRequest, String userId);

  void updateOrderStatus(String orderNumber, OrderStatus orderStatus);

  void delete(Long orderId);
}
