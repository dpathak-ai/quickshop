package com.dp.order_service.repository;

import com.dp.order_service.model.Order;
import com.dp.order_service.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Order> findByStatus(OrderStatus status);
    Optional<Order> findByIdAndUserId(Long id, Long userId);
}
