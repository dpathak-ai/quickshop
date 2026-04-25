package com.dp.product_service.dto.response;

import com.dp.product_service.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseV2 {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Integer stockHint;
    private Boolean active;
    private LocalDateTime createdAt;

    private String formattedPrice;
    private Boolean inStock;
    private String version;

    public static ProductResponseV2 from(Product product) {
        BigDecimal price = product.getPrice();
        Integer stockHint = product.getStockHint();

        return ProductResponseV2.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(price)
                .category(product.getCategory())
                .stockHint(stockHint)
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .formattedPrice(formatPrice(price))
                .inStock(stockHint != null && stockHint > 0)
                .version("v2")
                .build();
    }

    private static String formatPrice(BigDecimal price) {
        if (price == null) {
            return null;
        }
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        return format.format(price);
    }
}
