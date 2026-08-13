package com.ecommerce.order_service.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String aggregateId;
  private String type;

  @Column(columnDefinition = "TEXT")
  private String payload;

  private String status;
  private LocalDate created_at;
  private LocalDate processed_at;
  private boolean processed;
}
