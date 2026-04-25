package com.dp.product_service.controller;

import com.dp.product_service.dto.response.PagedResponse;
import com.dp.product_service.dto.response.ProductResponseV2;
import com.dp.product_service.model.Product;
import com.dp.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v2/products")
public class ProductControllerV2 {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponseV2>> getAllProductsV2(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy
    ) {
        log.info("GET /api/v2/products - page: {} size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        PagedResponse<ProductResponseV2> response = productService.getAllProductsV2(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseV2> getProductByIdV2(@PathVariable Long id) {
        String traceId = MDC.get("traceId");
        log.info("GET /api/v2/products/{} - traceId: {}", id, traceId);

        return ResponseEntity.ok(productService.getProductByIdV2(id));
    }
}
