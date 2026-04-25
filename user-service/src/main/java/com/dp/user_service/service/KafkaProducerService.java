package com.dp.user_service.service;

import com.dp.user_service.event.UserRegisteredEvent;
import com.dp.user_service.model.User;
import java.time.LocalDateTime;
import java.util.Objects;

import com.dp.user_service.util.TraceIdUtil;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private static final String USER_REGISTERED_TOPIC = "user.registered";

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public void publishUserRegistered(User user) {

        log.info("Publishing UserRegisteredEvent for userId={}", user.getId());
        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getId(),
                user.getEmail(),
                user.getName(),
                LocalDateTime.now().toString(),
                TraceIdUtil.getTraceId()
        );

        kafkaTemplate.send(USER_REGISTERED_TOPIC, String.valueOf(user.getId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish UserRegisteredEvent for userId={} to topic={}",
                                user.getId(), USER_REGISTERED_TOPIC, ex);
                    } else {
                        log.info("Published UserRegisteredEvent for userId={} to topic={}",
                                user.getId(), USER_REGISTERED_TOPIC);
                    }
                });
    }
}