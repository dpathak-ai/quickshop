package com.dp.product_service.repository;

import com.dp.product_service.model.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository  extends JpaRepository<Product, Long> {
    Page<Product> findByActiveTrue(Pageable pageable);
    List<Product> findByCategoryAndActiveTrue(String category);
    boolean existsByNameIgnoreCase(String name);
}
