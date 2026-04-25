package com.dp.order_service.service;

import com.dp.order_service.event.outbound.OrderPlacedEvent;
import com.dp.order_service.model.OutboxEvent;
import com.dp.order_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import tools.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxPollerService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.polling-interval-ms}")
    @Transactional
    public void pollAndPublish() {
        List<OutboxEvent> events = outboxEventRepository.findUnpublishedEvents();

        if (events.isEmpty()) {
            return;
        }

        log.debug("Outbox poller found {} unpublished events", events.size());

        for (OutboxEvent event : events) {
            try {
                if ("order.placed".equals(event.getEventType())) {
                    OrderPlacedEvent orderEvent = objectMapper.readValue(
                            event.getPayload(),
                            OrderPlacedEvent.class
                    );
                    kafkaProducerService.publishOrderPlaced(orderEvent);
                }

                outboxEventRepository.markAsPublished(event.getId(), LocalDateTime.now());

                log.info("Outbox event published - id: {} type: {} aggregateId: {}",
                        event.getId(), event.getEventType(), event.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event id: {} - will retry next poll",
                        event.getId(), e);
            }
        }
    }
}
