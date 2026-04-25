package com.dp.inventory_service.service;

import com.dp.inventory_service.dto.response.InventoryResponse;
import com.dp.inventory_service.event.OrderFailedEvent;
import com.dp.inventory_service.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.core.log.LogAccessor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaUtils;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.SerializationUtils;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;


@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private static final LogAccessor LOG_ACCESSOR = new LogAccessor(KafkaConsumerService.class);

    private final InventoryService inventoryService;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.placed", groupId = "inventory-service-group")
    public void handleOrderPlaced(ConsumerRecord<String, Object> record, Acknowledgment ack) {

        if (record.value() == null) {
            DeserializationException ex = SerializationUtils.getExceptionFromHeader(
                    record, KafkaUtils.VALUE_DESERIALIZER_EXCEPTION_HEADER, LOG_ACCESSOR);
            if (ex != null) {
                log.error("Deserialization failed for offset={}, partition={}, routing to DLT",
                        record.offset(), record.partition(), ex);
                throw ex; // non-retryable → DefaultErrorHandler sends straight to DLT
            }
            ack.acknowledge();
            return;
        }
        log.info("Received message on order.placed");
        OrderPlacedEvent event;
        try {
            event = objectMapper.convertValue(record.value(), OrderPlacedEvent.class);
        } catch (Exception e) {
            log.error("Failed to convert payload to OrderPlacedEvent", e);
            ack.acknowledge();
            return;
        }

        try {
            MDC.put("traceId", event.traceId());
            log.info("Received order.placed - orderId: {} productId: {} qty: {}",
                    event.orderId(), event.productId(), event.quantity());
            boolean reserved = inventoryService.reserveStock(event);

            if (reserved) {
                InventoryResponse response = inventoryService.getInventory(event.productId());
                kafkaProducerService.publishInventoryReserved(event, response.getAvailableQuantity());
                log.info("Published inventory.reserved - orderId: {}", event.orderId());
            } else {
                kafkaProducerService.publishInventoryFailed(event, "Insufficient stock");
                log.warn("Published inventory.failed - orderId: {} reason: insufficient stock",
                        event.orderId());
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process order.placed - orderId: {}", event.orderId(), e);
            throw e; // propagate — DefaultErrorHandler will retry, then DLT
        } finally {
            MDC.clear();
        }
    }

    @KafkaListener(topics = "order.failed", groupId = "inventory-service-group")
    public void handleOrderFailed(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        if (record.value() == null) {
            DeserializationException ex = SerializationUtils.getExceptionFromHeader(
                    record, KafkaUtils.VALUE_DESERIALIZER_EXCEPTION_HEADER, LOG_ACCESSOR);
            if (ex != null) {
                log.error("Deserialization failed for offset={}, partition={}, routing to DLT",
                        record.offset(), record.partition(), ex);
                throw ex; // non-retryable → DefaultErrorHandler sends straight to DLT
            }
            ack.acknowledge();
            return;
        }
        log.info("Received message on order.failed");
        OrderFailedEvent event;
        try {
            event = objectMapper.convertValue(record.value(), OrderFailedEvent.class);
        } catch (Exception e) {
            log.error("Failed to convert payload to OrderFailedEvent", e);
            ack.acknowledge();
            return;
        }

        try {
            MDC.put("traceId", event.traceId());
            log.info("Received order.failed - releasing stock for orderId: {}", event.orderId());
            inventoryService.releaseStock(event);
            log.info("Stock released for orderId: {}", event.orderId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to release stock for orderId: {}", event.orderId(), e);
            throw e; // propagate — DefaultErrorHandler will retry, then DLT
        } finally {
            MDC.clear();
        }
    }

}
