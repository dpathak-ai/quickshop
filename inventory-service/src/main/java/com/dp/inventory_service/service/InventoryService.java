package com.dp.inventory_service.service;

import com.dp.inventory_service.dto.request.UpdateStockRequest;
import com.dp.inventory_service.dto.response.InventoryResponse;
import com.dp.inventory_service.event.OrderFailedEvent;
import com.dp.inventory_service.event.OrderPlacedEvent;
import com.dp.inventory_service.exception.InventoryNotFoundException;
import com.dp.inventory_service.model.Inventory;
import com.dp.inventory_service.repository.InventoryRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDateTime;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class InventoryService {

    private static final String CACHE_PREFIX = "inventory::";

    private final InventoryRepository inventoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MeterRegistry meterRegistry;

    private Counter stockReservedCounter;
    private Counter stockFailedCounter;

    @PostConstruct
    public void initMetrics() {
        // Gauge — current total available stock across all products
        Gauge.builder("inventory.total.available", inventoryRepository,
                        repo -> repo.findAll().stream()
                                .mapToInt(i -> i.getQuantity() - i.getReserved())
                                .sum())
                .description("Total available stock across all products")
                .register(meterRegistry);

        // Counter — stock reservation attempts
        stockReservedCounter = Counter.builder("inventory.reservations.success")
                .description("Successful stock reservations")
                .register(meterRegistry);

        stockFailedCounter = Counter.builder("inventory.reservations.failed")
                .description("Failed stock reservations - insufficient stock")
                .register(meterRegistry);
    }

    @Cacheable(key = "#CACHE_PREFIX + #productId")
    public InventoryResponse getInventory(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(String.valueOf(productId)));

        return toResponse(inventory);
    }

    @Transactional
    @CacheEvict(key = "#CACHE_PREFIX + #productId")
    public InventoryResponse updateStock(Long productId, UpdateStockRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(String.valueOf(productId)));

        if (request.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity must be >= 0");
        }

        inventory.setQuantity(request.getQuantity());
        Inventory saved = inventoryRepository.save(inventory);

        redisTemplate.opsForValue().set(cacheKey(productId), toResponse(saved));
        log.info("Stock updated - productId: {} newQuantity: {}", productId, request.getQuantity());

        return toResponse(saved);
    }

    @Transactional
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public boolean reserveStock(OrderPlacedEvent event) {
        MDC.put("traceId", event.traceId());
        try {
            log.info("Reserving stock - orderId: {} productId: {} qty: {}",
                    event.orderId(), event.productId(), event.quantity());

            Inventory inventory = inventoryRepository.findByProductIdWithLock(event.productId())
                    .orElseThrow(() -> new InventoryNotFoundException(String.valueOf(event.productId())));

            if (!inventory.hasEnoughStock(event.quantity())) {
                log.warn("Insufficient stock - available: {} requested: {}",
                        inventory.getQuantity() - inventory.getReserved(), event.quantity());
                stockFailedCounter.increment();
                return false;
            }

            inventory.reserve(event.quantity());
            inventoryRepository.save(inventory);

            evictCache(event.productId());

            log.info("Stock reserved - orderId: {} remaining: {}",
                    event.orderId(), inventory.getQuantity() - inventory.getReserved());

            stockReservedCounter.increment();

            return true;
        } finally {
            MDC.remove("traceId");
        }

    }

    @Transactional
    public void releaseStock(OrderFailedEvent event) {
        MDC.put("traceId", event.traceId());
        try {
            log.info("Releasing reserved stock - orderId: {} productId: {}",
                    event.orderId(), event.productId());

            Inventory inventory = inventoryRepository.findByProductId(event.productId())
                    .orElseThrow(() -> new InventoryNotFoundException(String.valueOf(event.productId())));

            inventory.releaseStock(event.quantity());
            inventoryRepository.save(inventory);

            evictCache(event.productId());

            log.info("Stock released - productId: {} releasedQty: {}",
                    event.productId(), event.quantity());
        } finally {
            MDC.remove("traceId");
        }
    }

    private void evictCache(Long productId) {
        redisTemplate.delete(cacheKey(productId));
    }

    private String cacheKey(Long productId) {
        return CACHE_PREFIX + productId;
    }

    private InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getProductId(),
                inventory.getProductName(),
                inventory.getQuantity(),
                inventory.getReserved(),
                inventory.getAvailableQuantity(),
                LocalDateTime.now()
        );
    }
}
