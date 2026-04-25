package com.dp.inventory_service.event;

public record OrderFailedEvent(
        Long orderId,
        Long productId,
        Integer quantity,
        String failureReason,
        String traceId
) {
}
