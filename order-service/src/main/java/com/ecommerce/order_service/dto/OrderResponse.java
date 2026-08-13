package com.ecommerce.order_service.dto;

import com.ecommerce.order_service.model.OrderStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
  private Long id;
  private String orderNumber;
  private OrderStatus status;
  private List<OrderLineItemResponse> orderLineItemList;
}
