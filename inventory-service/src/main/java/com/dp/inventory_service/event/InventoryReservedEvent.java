package com.dp.inventory_service.event;

public record InventoryReservedEvent(
        Long    orderId,
        Long    productId,
        Integer quantity,
        Integer remainingStock,
        String  traceId
) {}
