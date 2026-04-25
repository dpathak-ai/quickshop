package com.dp.inventory_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    Long productId;
    String productName;
    Integer quantity;
    Integer reservedQuantity;
    Integer availableQuantity;
    LocalDateTime lastUpdated;
}
