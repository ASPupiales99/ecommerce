package com.ecommerce.order_service.mapper;

import com.ecommerce.order_service.dto.OrderLineItemRequest;
import com.ecommerce.order_service.dto.OrderLineItemResponse;
import com.ecommerce.order_service.dto.OrderRequest;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.model.Order;
import com.ecommerce.order_service.model.OrderLineItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

  Order toOrder(OrderRequest orderRequest);

  OrderLineItem toOrderLineItem(OrderLineItemRequest orderLineItemRequest);

  OrderResponse toOrderResponse(Order order);

  OrderLineItemResponse toOrderLineItemResponse(OrderLineItem orderLineItem);
}
