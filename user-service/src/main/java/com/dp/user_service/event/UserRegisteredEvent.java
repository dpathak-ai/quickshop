package com.dp.user_service.event;

import java.time.LocalDateTime;

public record UserRegisteredEvent(Long userId,
                                  String email,
                                  String name,
                                  String timestamp,
                                  String traceId) {

}