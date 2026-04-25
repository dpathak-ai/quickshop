package com.dp.order_service.event.outbound;

public record OrderPlacedEvent(
        Long    orderId,
        Long    userId,
        Long    productId,
        Integer quantity,
        String  traceId
) {
}
