package com.dp.inventory_service.event;

public record InventoryFailedEvent(
        Long    orderId,
        Long    productId,
        Integer requestedQuantity,
        Integer availableStock,
        String  failureReason,
        String  traceId
) {}