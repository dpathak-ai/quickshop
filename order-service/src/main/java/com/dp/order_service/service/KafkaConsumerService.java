package com.dp.order_service.service;

import com.dp.order_service.event.inbound.InventoryFailedEvent;
import com.dp.order_service.event.inbound.InventoryReservedEvent;
import tools.jackson.databind.ObjectMapper;
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

    private final OrderService orderService;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "inventory.reserved", groupId = "order-service-group")
    public void handleInventoryReserved(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            InventoryReservedEvent event = objectMapper.convertValue(record.value(), InventoryReservedEvent.class);
            MDC.put("traceId", event.traceId());
            log.info("Received inventory.reserved - orderId: {}", event.orderId());

            orderService.confirmOrder(event.orderId(), event.traceId());
            kafkaProducerService.publishOrderConfirmed(event);
            log.info("Order confirmed and event published - orderId: {}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to confirm order - orderId: {}", record.key(), e);
        } finally {
            MDC.clear();
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = "inventory.failed", groupId = "order-service-group")
    public void handleInventoryFailed(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            InventoryFailedEvent event = objectMapper.convertValue(record.value(), InventoryFailedEvent.class);
            MDC.put("traceId", event.traceId());
            log.warn("Received inventory.failed - orderId: {} reason: {}",
                    event.orderId(), event.failureReason());

            orderService.failOrder(event.orderId(), event.failureReason(), event.traceId());
            kafkaProducerService.publishOrderFailed(event);
            log.warn("Order failed and event published - orderId: {}", event.orderId());
        } catch (Exception e) {
            log.error("Failed to fail order - orderId: {}", record.key(), e);
        } finally {
            MDC.clear();
            ack.acknowledge();
        }
    }
}
