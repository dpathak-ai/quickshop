package com.dp.notification_service.service;

import com.dp.notification_service.event.OrderConfirmedEvent;
import com.dp.notification_service.event.OrderFailedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final NotificationService notificationService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.confirmed", groupId = "notification-service-group")
    public void handleOrderConfirmed(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            Object payload = record.value();
            OrderConfirmedEvent event = objectMapper.convertValue(payload, OrderConfirmedEvent.class);

            MDC.put("traceId", event.traceId() != null ? event.traceId() : "no-trace");
            log.info("Received order.confirmed - orderId: {}", event.orderId());

            String eventId = idempotencyService.buildEventId("order.confirmed", event.orderId());

            if (idempotencyService.isAlreadyProcessed(eventId)) {
                log.warn("Duplicate event detected - skipping. eventId: {}", eventId);
                return;
            }

            notificationService.sendOrderConfirmedNotification(event);
            idempotencyService.markAsProcessed(eventId);

            log.info("Order confirmed notification sent - orderId: {}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to process order.confirmed - offset: {}", record.offset(), e);
        } finally {
            MDC.clear();
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = "order.failed", groupId = "notification-service-group")
    public void handleOrderFailed(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            Object payload = record.value();
            OrderFailedEvent event = objectMapper.convertValue(payload, OrderFailedEvent.class);

            MDC.put("traceId", event.traceId() != null ? event.traceId() : "no-trace");
            log.warn("Received order.failed - orderId: {}", event.orderId());

            String eventId = idempotencyService.buildEventId("order.failed", event.orderId());

            if (idempotencyService.isAlreadyProcessed(eventId)) {
                log.warn("Duplicate event detected - skipping. eventId: {}", eventId);
                return;
            }

            notificationService.sendOrderFailedNotification(event);
            idempotencyService.markAsProcessed(eventId);

            log.warn("Order failed notification sent - orderId: {}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to process order.failed - offset: {}", record.offset(), e);
        } finally {
            MDC.clear();
            ack.acknowledge();
        }
    }

    @KafkaListener(
            topics = {"order.confirmed.DLT", "order.failed.DLT"},
            groupId = "notification-service-dlq-group"
    )
    public void handleDeadLetter(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            log.error("DLT message received - topic: {} offset: {} value: {}",
                    record.topic(), record.offset(), record.value());
            // In a real system: store in dead_letters table, alert team, trigger PagerDuty, etc.
        } finally {
            ack.acknowledge();
        }
    }
}
