package com.dp.order_service.config;

import com.dp.order_service.dto.response.ProductResponse;
import com.dp.order_service.exception.ProductServiceException;
import com.dp.order_service.feign.ProductServiceClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductServiceClientFallbackFactory implements FallbackFactory<ProductServiceClient> {
    @Override
    public ProductServiceClient create(Throwable cause) {
        return new ProductServiceClient() {
            @Override
            public ProductResponse getProductById(Long id) {
                throw new ProductServiceException(
                        "Product service unavailable: " + (cause != null ? cause.getMessage() : "unknown error")
                );
            }
        };
    }
}
