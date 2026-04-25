package com.dp.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class IdempotencyService {

    private static final String IDEMPOTENCY_PREFIX = "notif:processed:";
    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, String> redisTemplate;

    public boolean isAlreadyProcessed(String eventId) {
        String key = IDEMPOTENCY_PREFIX + eventId;
        Boolean hasKey = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(hasKey);
    }

    public void markAsProcessed(String eventId) {
        String key = IDEMPOTENCY_PREFIX + eventId;
        redisTemplate.opsForValue().set(key, "1", TTL);
        log.debug("Marked as processed - eventId: {}", eventId);
    }

    public String buildEventId(String topic, Long orderId) {
        return topic + ":" + orderId;
    }
}
