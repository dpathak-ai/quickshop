package com.dp.notification_service.service;

import com.dp.notification_service.event.OrderConfirmedEvent;
import com.dp.notification_service.event.OrderFailedEvent;
import com.dp.notification_service.model.NotificationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final ObjectMapper objectMapper;

    public void sendOrderConfirmedNotification(OrderConfirmedEvent event) {
        NotificationLog notification = NotificationLog.builder()
                .type("ORDER_CONFIRMED")
                .orderId(event.orderId())
                .userId(event.userId())
                .channel("EMAIL")
                .recipient("user-" + event.userId() + "@quickshop.com")
                .subject("Your order #" + event.orderId() + " is confirmed!")
                .body("Hi! Your order for " + event.quantity() + " item(s) has been confirmed.")
                .traceId(event.traceId())
                .timestamp(LocalDateTime.now().toString())
                .build();

        try {
            log.info("NOTIFICATION SENT: {}", objectMapper.writeValueAsString(notification));
        } catch (JacksonException e) {
            log.error("Failed to serialize confirmed notification log - orderId: {}", event.orderId(), e);
        }
    }

    public void sendOrderFailedNotification(OrderFailedEvent event) {
        NotificationLog notification = NotificationLog.builder()
                .type("ORDER_FAILED")
                .orderId(event.orderId())
                .userId(null)
                .channel("EMAIL")
                .recipient("user@quickshop.com")
                .subject("Your order #" + event.orderId() + " could not be processed")
                .body("Sorry! " + event.failureReason())
                .traceId(event.traceId())
                .timestamp(LocalDateTime.now().toString())
                .build();

        try {
            log.warn("NOTIFICATION SENT: {}", objectMapper.writeValueAsString(notification));
        } catch (JacksonException e) {
            log.error("Failed to serialize failed notification log - orderId: {}", event.orderId(), e);
        }
    }
}
