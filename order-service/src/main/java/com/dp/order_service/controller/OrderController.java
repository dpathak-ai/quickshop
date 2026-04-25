package com.dp.order_service.controller;

import com.dp.order_service.dto.request.CreateOrderRequest;
import com.dp.order_service.dto.response.OrderResponse;
import com.dp.order_service.service.JwtService;
import com.dp.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        String token = extractToken(authorizationHeader);
        Long userId = jwtService.extractUserId(token);

        log.info("POST /orders - userId: {} productId: {}",
                userId, request.getProductId());

        OrderResponse response = orderService.createOrder(request, userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long id
    ) {
        String token = extractToken(authorizationHeader);
        Long userId = jwtService.extractUserId(token);

        OrderResponse response = orderService.getOrderById(id, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long userId
    ) {
        String token = extractToken(authorizationHeader);
        Long authenticatedUserId = jwtService.extractUserId(token);

        if (!authenticatedUserId.equals(userId)) {
            throw new SecurityException("Access denied: cannot view another user's orders");
        }

        List<OrderResponse> orders = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(orders);
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        return authorizationHeader.substring(7);
    }
}
