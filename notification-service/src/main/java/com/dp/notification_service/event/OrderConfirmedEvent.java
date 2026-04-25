package com.dp.notification_service.event;

public record OrderConfirmedEvent(
        Long    orderId,
        Long    userId,
        Long    productId,
        Integer quantity,
        String  traceId
) {
}
