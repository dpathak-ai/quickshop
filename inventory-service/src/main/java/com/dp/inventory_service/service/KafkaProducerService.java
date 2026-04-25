package com.dp.inventory_service.service;

import com.dp.inventory_service.dto.response.InventoryResponse;
import com.dp.inventory_service.event.InventoryFailedEvent;
import com.dp.inventory_service.event.InventoryReservedEvent;
import com.dp.inventory_service.event.OrderPlacedEvent;
import com.dp.inventory_service.util.TraceIdUtil;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final InventoryService inventoryService;

    public void publishInventoryReserved(OrderPlacedEvent order, int remainingStock) {
        InventoryReservedEvent event = new InventoryReservedEvent(
                order.orderId(),
                order.productId(),
                order.quantity(),
                remainingStock,
                TraceIdUtil.getTraceId()
        );

        kafkaTemplate.send("inventory.reserved", order.orderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Published inventory.reserved - orderId: {}", order.orderId());
                    } else {
                        log.error("Failed to publish inventory.reserved - orderId: {}", order.orderId(), ex);
                    }
                });
    }

    public void publishInventoryFailed(OrderPlacedEvent order, String reason) {
        Integer availableStock = 0;
        try {
            InventoryResponse inventory = inventoryService.getInventory(order.productId());
            availableStock = inventory.getAvailableQuantity();
        } catch (Exception ignored) {
            // Keep 0 if inventory cannot be resolved while publishing failure event
        }

        InventoryFailedEvent event = new InventoryFailedEvent(
                order.orderId(),
                order.productId(),
                order.quantity(),
                availableStock,
                reason,
                TraceIdUtil.getTraceId()
        );

        kafkaTemplate.send("inventory.failed", order.orderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Published inventory.failed - orderId: {}", order.orderId());
                    } else {
                        log.error("Failed to publish inventory.failed - orderId: {}", order.orderId(), ex);
                    }
                });
    }
}
