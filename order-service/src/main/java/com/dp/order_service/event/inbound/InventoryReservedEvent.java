package com.dp.order_service.event.inbound;

public record InventoryReservedEvent(
        Long    orderId,
        Long    productId,
        Integer quantity,
        Integer remainingStock,
        String  traceId
) {
}
