package com.dp.order_service.event.inbound;

public record InventoryFailedEvent(
        Long    orderId,
        Long    productId,
        Integer requestedQuantity,
        Integer availableStock,
        String  failureReason,
        String  traceId
) {
}
