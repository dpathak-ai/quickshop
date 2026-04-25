package com.dp.notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    private String type;       // "ORDER_CONFIRMED" or "ORDER_FAILED"
    private Long orderId;
    private Long userId;       // nullable for failed events
    private String channel;    // always "EMAIL"
    private String recipient;  // user-{userId}@quickshop.com
    private String subject;
    private String body;
    private String traceId;
    private String timestamp;  // ISO formatted
}
