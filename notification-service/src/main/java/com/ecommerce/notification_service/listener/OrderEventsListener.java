package com.ecommerce.notification_service.listener;

import com.ecommerce.notification_service.event.OrderCancelledEvent;
import com.ecommerce.notification_service.event.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@RabbitListener(queues = "notification-queue")
public class OrderEventsListener {

  private final JavaMailSender mailSender;

  @RabbitHandler
  public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {

    log.info("Confirmation event for order: {}", event.orderNumber());

    SimpleMailMessage message = createMessage(event);

    mailSender.send(message);

    log.info("Sending notification by mail to {}", event.email());

    log.info("Notification for order {} was sent successfully", event.orderNumber());
  }

  @RabbitHandler
  public void handleOrderCancelledEvent(OrderCancelledEvent event) {

    log.info("Cancelled event for order: {}", event.orderNumber());

    SimpleMailMessage message = createCancellationMessage(event);
    mailSender.send(message);

    log.info(
        "Cancellation notification for order {} was sent successfully to {}",
        event.orderNumber(),
        event.email());
  }

  private static @NonNull SimpleMailMessage createMessage(OrderConfirmedEvent event) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("pedidos@ecommerce.com");
    message.setTo(event.email());
    message.setSubject("Order Placed - " + event.orderNumber());
    message.setText(
        "Hi!\n\n"
            + "Your order: "
            + event.orderNumber()
            + " has been placed successfully.\n\n"
            + "You'll hear from us soon regarding the shipment of your order.\n\n"
            + "Thank you for shopping with us.");
    return message;
  }

  private static @NonNull SimpleMailMessage createCancellationMessage(OrderCancelledEvent event) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("pedidos@ecommerce.com");
    message.setTo(event.email());
    message.setSubject("Order Update - " + event.orderNumber());
    message.setText(
        "Hi!\n\n"
            + "We regret to inform you that your order: "
            + event.orderNumber()
            + " has been canceled.\n\n"
            + "Reason: "
            + event.reason()
            + "\n\n"
            + "If any charges were made, they will be refunded as soon as possible.");
    return message;
  }
}
