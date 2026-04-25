package com.dp.order_service.service;

import com.dp.order_service.event.inbound.InventoryFailedEvent;
import com.dp.order_service.event.inbound.InventoryReservedEvent;
import com.dp.order_service.event.outbound.OrderConfirmedEvent;
import com.dp.order_service.event.outbound.OrderFailedEvent;
import com.dp.order_service.event.outbound.OrderPlacedEvent;
import com.dp.order_service.util.TraceIdUtil;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderPlaced(OrderPlacedEvent event) {
        String topic = "order.placed";
        String key = event.orderId().toString();

        log.info("Publishing order.placed - orderId: {}", event.orderId());

        // Build event with traceId included
        OrderPlacedEvent enriched = new OrderPlacedEvent(
                event.orderId(),
                event.userId(),
                event.productId(),
                event.quantity(),
                event.traceId()       // ← traceId travels with event
        );

        kafkaTemplate.send(topic, key, enriched)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish order.placed - orderId: {}", enriched.orderId(), ex);
                    } else {
                        log.info("Published order.placed - orderId: {} topic: {}", enriched.orderId(), topic);
                    }
                });
    }

    public void publishOrderConfirmed(InventoryReservedEvent event) {
        OrderConfirmedEvent orderConfirmedEvent = new OrderConfirmedEvent(
                event.orderId(),
                null,
                event.productId(),
                event.quantity(),
                event.traceId()
        );

        String topic = "order.confirmed";
        String key = event.orderId().toString();

        log.info("Publishing order.confirmed - orderId: {}", event.orderId());

        kafkaTemplate.send(topic, key, orderConfirmedEvent)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish order.confirmed - orderId: {}", event.orderId(), ex);
                    } else {
                        log.info("Published order.confirmed - orderId: {} topic: {}", event.orderId(), topic);
                    }
                });
    }

    public void publishOrderFailed(InventoryFailedEvent event) {
        OrderFailedEvent orderFailedEvent = new OrderFailedEvent(
                event.orderId(),
                event.productId(),
                event.requestedQuantity(),
                event.failureReason(),
                event.traceId()
        );

        String topic = "order.failed";
        String key = event.orderId().toString();

        log.info("Publishing order.failed - orderId: {}", event.orderId());

        kafkaTemplate.send(topic, key, orderFailedEvent)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish order.failed - orderId: {}", event.orderId(), ex);
                    } else {
                        log.info("Published order.failed - orderId: {} topic: {}", event.orderId(), topic);
                    }
                });
    }
}