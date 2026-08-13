package com.ecommerce.order_service.service.impl;

import com.ecommerce.order_service.event.OrderPlacedEvent;
import com.ecommerce.order_service.model.OutboxEvent;
import com.ecommerce.order_service.repository.OutboxEventRepository;
import com.ecommerce.order_service.service.OutboxEventService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventServiceImpl implements OutboxEventService {

  private final OutboxEventRepository repository;
  private final ObjectMapper objectMapper;

  @Override
  public void saveOrderPlacedEvent(OrderPlacedEvent event, boolean isProcessed) {

    String payload = objectMapper.writeValueAsString(event);

    OutboxEvent outboxEvent =
        OutboxEvent.builder()
            .aggregateId(event.orderNumber())
            .type("ORDER_PLACED")
            .payload(payload)
            .created_at(LocalDate.now())
            .processed(isProcessed)
            .build();

    repository.save(outboxEvent);

    log.info("Event saved in Outbox: {}", event.orderNumber());
  }

  @Override
  public List<OutboxEvent> getPendingEvents() {
    return repository.findByProcessedFalse();
  }

  @Override
  public void markAsProcessed(Long id) {

    repository
        .findById(id)
        .ifPresent(
            event -> {
              event.setProcessed(true);
              event.setProcessed_at(LocalDate.now());
              repository.save(event);
              log.info("Event {} marked as processed", id);
            });
  }
}
