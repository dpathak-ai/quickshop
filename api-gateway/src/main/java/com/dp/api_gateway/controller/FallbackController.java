package com.dp.api_gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Slf4j
public class FallbackController {

    @GetMapping("/fallback/user-service")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        log.warn("User service circuit breaker OPEN — returning fallback");
        return buildFallbackResponse("user-service");
    }

    @GetMapping("/fallback/product-service")
    public ResponseEntity<Map<String, Object>> productServiceFallback() {
        log.warn("Product service circuit breaker OPEN — returning fallback");
        return buildFallbackResponse("product-service");
    }

    @GetMapping("/fallback/order-service")
    public ResponseEntity<Map<String, Object>> orderServiceFallback() {
        log.warn("Order service circuit breaker OPEN — returning fallback");
        return buildFallbackResponse("order-service");
    }

    @GetMapping("/fallback/inventory-service")
    public ResponseEntity<Map<String, Object>> inventoryServiceFallback() {
        log.warn("Inventory service circuit breaker OPEN — returning fallback");
        return buildFallbackResponse("inventory-service");
    }

    private ResponseEntity<Map<String, Object>> buildFallbackResponse(String service) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 503);
        body.put("error", "Service Unavailable");
        body.put("message", service + " is currently unavailable. Please try again.");
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}