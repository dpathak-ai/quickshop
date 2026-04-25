package com.dp.inventory_service.controller;

import com.dp.inventory_service.dto.request.UpdateStockRequest;
import com.dp.inventory_service.dto.response.InventoryResponse;
import com.dp.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
@Slf4j
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    public InventoryResponse getInventory(@PathVariable Long productId) {
        log.info("GET /inventory/{}", productId);
        return inventoryService.getInventory(productId);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public InventoryResponse updateStock(
            @PathVariable Long productId,
            @RequestBody @Valid UpdateStockRequest request
    ) {
        log.info("PUT /inventory/{} - newQty: {}", productId, request.getQuantity());
        return inventoryService.updateStock(productId, request);
    }
}
