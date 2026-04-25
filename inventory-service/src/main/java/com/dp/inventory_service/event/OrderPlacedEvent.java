package com.dp.inventory_service.event;

public record OrderPlacedEvent(
        Long orderId,
        Long userId,
        Long productId,
        Integer quantity,
        String traceId
) {
}
