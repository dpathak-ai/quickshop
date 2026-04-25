package com.dp.order_service.event.outbound;

public record OrderFailedEvent(
        Long    orderId,
        Long    productId,
        Integer quantity,
        String  failureReason,
        String  traceId
) {
}
