package com.dp.inventory_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer reserved;

    @Version
    private Long version;

    public boolean hasEnoughStock(int requestedQuantity) {
        return quantity - reserved >= requestedQuantity;
    }

    public void reserve(int quantity) {
        this.reserved += quantity;
        if(this.reserved > this.quantity) {
            throw new IllegalStateException("Not enough stock");
        }
    }

    public void releaseStock(int quantity) {
        this.reserved -= quantity;
        if(this.reserved < 0) {
            throw new IllegalStateException("Reserved quantity cannot be negative");
        }
    }

    public void confirmReservation(int quantity) {
        this.quantity -= quantity;
        this.reserved -= quantity;
    }

    public Integer getAvailableQuantity() {
        return quantity - reserved;
    }


}
