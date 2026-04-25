package com.dp.inventory_service.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public JsonMapper objectMapper() {
        return JsonMapper.builder()
                // Ignore unknown fields — safe for event schema evolution
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

                // Don't fail on empty beans
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

                // JavaTimeModule is BUILT IN to Jackson 3 — no manual registration needed
                // LocalDateTime, Instant, LocalDate all work out of the box
                .build();  // ← immutable after this point


    }
}
