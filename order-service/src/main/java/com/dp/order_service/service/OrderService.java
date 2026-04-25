package com.dp.order_service.service;

import com.dp.order_service.dto.request.CreateOrderRequest;
import com.dp.order_service.dto.response.OrderResponse;
import com.dp.order_service.dto.response.ProductResponse;
import com.dp.order_service.event.outbound.OrderPlacedEvent;
import com.dp.order_service.exception.OrderNotFoundException;
import com.dp.order_service.feign.ProductServiceClient;
import com.dp.order_service.model.Order;
import com.dp.order_service.model.OrderStatus;
import com.dp.order_service.model.OutboxEvent;
import com.dp.order_service.repository.OrderRepository;
import com.dp.order_service.repository.OutboxEventRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ProductServiceClient productServiceClient;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    // Counter — total orders placed
    private Counter ordersPlacedCounter;
    private Counter ordersConfirmedCounter;
    private Counter ordersFailedCounter;

    // Timer — how long order creation takes
    private Timer orderCreationTimer;

    @PostConstruct
    public void initMetrics() {
        ordersPlacedCounter = Counter.builder("orders.placed.total")
                .description("Total number of orders placed")
                .register(meterRegistry);

        ordersConfirmedCounter = Counter.builder("orders.confirmed.total")
                .description("Total number of orders confirmed")
                .register(meterRegistry);

        ordersFailedCounter = Counter.builder("orders.failed.total")
                .description("Total number of orders failed")
                .register(meterRegistry);

        orderCreationTimer = Timer.builder("orders.creation.duration")
                .description("Time taken to create an order")
                .register(meterRegistry);
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Long userId) {
        return orderCreationTimer.record(() -> {
        log.info("Creating order - userId: {} productId: {} qty: {}",
                userId, request.getProductId(), request.getQuantity());

        ProductResponse product = productServiceClient.getProductById(request.getProductId());
        log.debug("Product fetched: {} price: {}", product.getName(), product.getPrice());

        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder()
                .userId(userId)
                .productId(request.getProductId())
                .productName(product.getName())
                .quantity(request.getQuantity())
                .unitPrice(product.getPrice())
                .totalPrice(totalPrice)
                .status(OrderStatus.PENDING)
                .traceId(MDC.get("traceId"))
                .build();

        Order savedOrder = orderRepository.save(order);

        OrderPlacedEvent event = new OrderPlacedEvent(
                savedOrder.getId(),
                userId,
                request.getProductId(),
                request.getQuantity(),
                MDC.get("traceId")
        );

        String payload = objectMapper.writeValueAsString(event);

        OutboxEvent outbox = OutboxEvent.builder()
                .aggregateId(savedOrder.getId().toString())
                .eventType("order.placed")
                .payload(payload)
                .published(false)
                .build();

        outboxEventRepository.save(outbox);

        log.info("Order created - orderId: {} status: PENDING traceId: {}",
                savedOrder.getId(), MDC.get("traceId"));

        ordersPlacedCounter.increment();
        return mapToResponse(savedOrder);
        });
    }

    @Transactional
    public void confirmOrder(Long orderId, String traceId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order {} already in status: {}", orderId, order.getStatus());
            return;
        }

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        log.info("Order confirmed - orderId: {}", orderId);
        ordersConfirmedCounter.increment();
    }

    public void failOrder(Long orderId, String reason, String traceId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order {} already in status: {}", orderId, order.getStatus());
            return;
        }

        order.setStatus(OrderStatus.FAILED);
        order.setFailureReason(reason);
        orderRepository.save(order);

        log.warn("Order failed - orderId: {} reason: {}", orderId, reason);
        ordersFailedCounter.increment();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .productId(order.getProductId())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .failureReason(order.getFailureReason())
                .traceId(order.getTraceId())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
