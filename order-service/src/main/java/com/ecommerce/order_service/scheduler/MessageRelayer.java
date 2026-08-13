package com.ecommerce.order_service.scheduler;

import com.ecommerce.order_service.event.OrderPlacedEvent;
import com.ecommerce.order_service.model.OutboxEvent;
import com.ecommerce.order_service.service.OutboxEventService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageRelayer {

  private final RabbitTemplate rabbitTemplate;
  private final OutboxEventService service;
  private final ObjectMapper objectMapper;

  @Scheduled(fixedDelay = 10000)
  public void relayMessage() {

    List<OutboxEvent> pendingEventList = service.getPendingEvents();

    if (!pendingEventList.isEmpty()) {

      log.info("Relayer detected {} pending events", pendingEventList.size());

      for (OutboxEvent event : pendingEventList) {

        try {

          OrderPlacedEvent originalEvent =
              objectMapper.readValue(event.getPayload(), OrderPlacedEvent.class);
          rabbitTemplate.convertAndSend("order-events", "order.placed", originalEvent);
          service.markAsProcessed(event.getId());

          log.info("Event {} marked as processed and sent", event.getAggregateId());

        } catch (JacksonException e) {

          log.error(
              "Could not relay event {} due to JacksonException: {}",
              event.getId(),
              e.getMessage());

        } catch (AmqpException e) {

          log.error(
              "Error while relaying order event {}: {}", event.getAggregateId(), e.getMessage());
        }
      }
    }
  }
}
