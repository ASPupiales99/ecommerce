package com.ecommerce.order_service.service.impl;

import com.ecommerce.order_service.dto.OrderRequest;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.event.OrderPlacedEvent;
import com.ecommerce.order_service.exception.ResourceNotFoundException;
import com.ecommerce.order_service.mapper.OrderMapper;
import com.ecommerce.order_service.model.Order;
import com.ecommerce.order_service.model.OrderStatus;
import com.ecommerce.order_service.repository.OrderRepository;
import com.ecommerce.order_service.service.OrderService;
import com.ecommerce.order_service.service.OutboxEventService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@RefreshScope
public class OrderServiceImpl implements OrderService {

  private final OrderRepository repository;
  private final OrderMapper mapper;
  private final RabbitTemplate rabbitTemplate;
  private final OutboxEventService outboxEventService;

  @Value("${order.enabled:true}")
  private boolean ordersEnabled;

  @Override
  @Transactional(readOnly = true)
  public OrderResponse findById(Long orderId) {

    Order order =
        repository
            .findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

    return mapper.toOrderResponse(order);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderResponse> findAll() {
    return repository.findAll().stream().map(mapper::toOrderResponse).collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderResponse> getOrderHistory(String userId, boolean isAdmin) {

    List<Order> orders;

    if (isAdmin) {
      orders = repository.findAll();
    } else {
      orders = repository.findByUserId(userId);
    }

    return orders.stream().map(mapper::toOrderResponse).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public OrderResponse placeOrder(OrderRequest orderRequest, String userId) {

    if (!ordersEnabled) {
      log.warn("Request Rejected: Service disabled due to configuration");
      throw new RuntimeException(
          "The ordering service is currently undergoing maintenance. Please try again later.");
    }

    log.info("Placing new order");

    Order order = mapper.toOrder(orderRequest);
    order.setUserId(userId);
    order.setOrderNumber(UUID.randomUUID().toString());
    order.setStatus(OrderStatus.PLACED);
    Order placedOrder = repository.save(order);

    log.info("Order with ID: {} placed successfully", placedOrder.getId());

    List<OrderPlacedEvent.OrderItemEvent> orderItemList =
        order.getOrderLineItemList().stream()
            .map(
                item ->
                    new OrderPlacedEvent.OrderItemEvent(
                        item.getSku(), item.getPrice().toString(), item.getQuantity()))
            .toList();

    OrderPlacedEvent event =
        new OrderPlacedEvent(placedOrder.getOrderNumber(), orderRequest.getEmail(), orderItemList);

    boolean sendToRabbit = false;

    try {

      rabbitTemplate.convertAndSend("order-events", "order.placed", event);
      sendToRabbit = true;
      log.info("Order {} sent to Rabbit", placedOrder.getOrderNumber());

    } catch (AmqpException e) {

      log.error("Error while sending order {} to RabbitMQ", event.orderNumber());
    }

    outboxEventService.saveOrderPlacedEvent(event, sendToRabbit);
    log.info("Order: {} placed successfully", placedOrder.getOrderNumber());

    return mapper.toOrderResponse(placedOrder);
  }

  @Override
  @Transactional
  public void updateOrderStatus(String orderNumber, OrderStatus orderStatus) {
    repository
        .findByOrderNumber(orderNumber)
        .ifPresentOrElse(
            order -> {
              order.setStatus(orderStatus);
              repository.save(order);
              log.info("Order status updated in DB for order {}", orderNumber);
            },
            () -> log.error("Order {} could not be found in DB", orderNumber));
  }

  @Override
  @Transactional
  public void delete(Long orderId) {

    if (!repository.existsById(orderId)) {
      throw new ResourceNotFoundException("Order", "id", orderId);
    }

    repository.deleteById(orderId);
    log.info("Order with ID: {} deleted successfully", orderId);
  }

  public OrderResponse fallbackMethod(OrderRequest orderRequest, String userId, Throwable ex) {
    log.error("Fallback Method has been called: Circuit breaker activated {}", ex.getMessage());
    throw new RuntimeException("Inventory service is not responding. Please try again later.");
  }
}
