package com.n11bootcamp.order_service.repository;

import com.n11bootcamp.order_service.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}

