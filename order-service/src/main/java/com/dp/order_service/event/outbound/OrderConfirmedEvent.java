package com.dp.order_service.event.outbound;

public record OrderConfirmedEvent(
        Long    orderId,
        Long    userId,
        Long    productId,
        Integer quantity,
        String  traceId
) {
}
