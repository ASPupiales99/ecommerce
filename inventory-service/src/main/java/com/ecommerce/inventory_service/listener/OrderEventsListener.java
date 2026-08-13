package com.ecommerce.inventory_service.listener;

import com.ecommerce.inventory_service.event.OrderCancelledEvent;
import com.ecommerce.inventory_service.event.OrderConfirmedEvent;
import com.ecommerce.inventory_service.event.OrderPlacedEvent;
import com.ecommerce.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class OrderEventsListener {

  private final InventoryService inventoryService;
  private final RabbitTemplate rabbitTemplate;

  @RabbitListener(queues = "inventory-queue")
  public void handleOrderPlacedEvent(OrderPlacedEvent event) {

    log.info("Received inventory event for order: {}", event.orderNumber());

    try {

      boolean allProductsInStock =
          event.items().stream()
              .allMatch(item -> inventoryService.inInStock(item.sku(), item.quantity()));

      if (!allProductsInStock) {
        cancelOrder(event, "One or more products do not have enough stock");
        return;
      }

      event
          .items()
          .forEach(
              item -> {
                inventoryService.reduceStock(item.sku(), item.quantity());
              });

      OrderConfirmedEvent confirmedEvent =
          new OrderConfirmedEvent(event.orderNumber(), event.email());

      rabbitTemplate.convertAndSend("order-events", "order.confirmed", confirmedEvent);

      log.info("Stock reduced successfully for order {}", event.orderNumber());
    } catch (Exception e) {
      log.error("Unexpected error while reducing stock: {}", e.getMessage());
      cancelOrder(event, "Error while reducing the stock: " + e.getMessage());
    }
  }

  private void cancelOrder(OrderPlacedEvent event, String reason) {
    OrderCancelledEvent cancelledEvent =
        new OrderCancelledEvent(event.orderNumber(), event.email(), reason);

    rabbitTemplate.convertAndSend("order-events", "order.cancelled", cancelledEvent);
  }
}
