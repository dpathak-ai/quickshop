package com.dp.product_service.service;

import com.dp.product_service.dto.request.CreateProductRequest;
import com.dp.product_service.dto.response.PagedResponse;
import com.dp.product_service.dto.response.ProductResponse;
import com.dp.product_service.dto.response.ProductResponseV2;
import com.dp.product_service.exception.ProductNotFoundException;
import com.dp.product_service.model.Product;
import com.dp.product_service.repository.ProductRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MeterRegistry meterRegistry;
    private Counter cacheHitCounter;
    private Counter cacheMissCounter;

    @PostConstruct
    public void initMetrics() {
        cacheHitCounter = Counter.builder("product.cache.hits")
                .description("Redis cache hits for product lookups")
                .register(meterRegistry);

        cacheMissCounter = Counter.builder("product.cache.misses")
                .description("Redis cache misses for product lookups")
                .register(meterRegistry);
    }

    @Cacheable(value = "product", key = "#id")
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {

        cacheMissCounter.increment();
        log.debug("Fetching product from DB - id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }

        return toResponse(product);
    }

    @Cacheable(value = "products", key = "'all-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getAllProducts(Pageable pageable) {
        log.debug("Fetching all products from DB - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> page = productRepository.findByActiveTrue(pageable);

        List<ProductResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PagedResponse.<ProductResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Cacheable(value = "products", key = "'v2-all-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponseV2> getAllProductsV2(Pageable pageable) {
        Page<Product> page = productRepository.findByActiveTrue(pageable);

        List<ProductResponseV2> content = page.getContent().stream()
                .map(this::toResponseV2)
                .toList();

        return PagedResponse.<ProductResponseV2>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Cacheable(value = "product", key = "#id")
    @Transactional(readOnly = true)
    public ProductResponseV2 getProductByIdV2(Long id) {
        log.debug("Fetching product(v2) from DB - id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }

        return toResponseV2(product);
    }

    @CacheEvict(value = {"products", "categories"}, allEntries = true)
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsByNameIgnoreCase(request.getName())) {
            log.warn("Product creation skipped - duplicate name: {}", request.getName());
            throw new IllegalArgumentException("Product with this name already exists");
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .stockHint(request.getStockHint())
                .active(true)
                .build();

        Product saved = productRepository.save(product);

        log.info("Product created - id: {} name: {}", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @CacheEvict(value = "product", key = "#id")
    @Transactional
    public void deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        product.setActive(false);
        productRepository.save(product);

        redisTemplate.delete("products::all-0-20");
        redisTemplate.delete("categories::all");

        log.info("Product deactivated - id: {}", id);
    }

    @Cacheable(value = "categories", key = "'all'")
    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return productRepository.findAll().stream()
                .filter(product -> Boolean.TRUE.equals(product.getActive()))
                .map(Product::getCategory)
                .distinct()
                .toList();
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .stockHint(product.getStockHint())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .build();
    }

    private ProductResponseV2 toResponseV2(Product product) {
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
                .formattedPrice(price == null ? null : "$" + price)
                .inStock(stockHint != null && stockHint > 0)
                .version("v2")
                .build();
    }
}
