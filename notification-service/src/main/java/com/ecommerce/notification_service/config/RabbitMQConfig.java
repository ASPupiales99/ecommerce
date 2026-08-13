package com.ecommerce.notification_service.config;

import com.ecommerce.notification_service.event.OrderCancelledEvent;
import com.ecommerce.notification_service.event.OrderConfirmedEvent;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String EXCHANGE_NAME = "order-events";

  @Bean
  public MessageConverter messageConverter() {

    JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
    DefaultClassMapper classMapper = new DefaultClassMapper();

    classMapper.setTrustedPackages("*");

    Map<String, Class<?>> idClassMapping = new HashMap<>();

    idClassMapping.put(
        "com.ecommerce.inventory_service.event.OrderConfirmedEvent", OrderConfirmedEvent.class);
    idClassMapping.put(
        "com.ecommerce.inventory_service.event.OrderCancelledEvent", OrderCancelledEvent.class);

    classMapper.setIdClassMapping(idClassMapping);
    converter.setClassMapper(classMapper);

    return converter;
  }

  @Bean
  public Queue notificationQueue() {
    return QueueBuilder.durable("notification-queue")
        .withArgument("x-dead-letter-exchange", "notification-dlx")
        .withArgument("x-dead-letter-routing-key", "notification.dead")
        .build();
  }

  @Bean
  public TopicExchange orderEventsExchange() {
    return new TopicExchange(EXCHANGE_NAME);
  }

  @Bean
  public Binding binding(Queue notificationQueue, TopicExchange orderEventsExchange) {
    return BindingBuilder.bind(notificationQueue).to(orderEventsExchange).with("order.confirmed");
  }

  @Bean
  public Binding cancellationBinding(Queue notificationQueue, TopicExchange orderEventsExchange) {
    return BindingBuilder.bind(notificationQueue).to(orderEventsExchange).with("order.cancelled");
  }

  @Bean
  public DirectExchange deadLetterExchange() {
    return new DirectExchange("notification-dlx");
  }

  @Bean
  Queue deadLetterQueue() {
    return new Queue("notification-dlq", true);
  }

  @Bean
  public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
    return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("notification.dead");
  }
}
