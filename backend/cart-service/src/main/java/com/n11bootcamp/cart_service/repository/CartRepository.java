package com.n11bootcamp.cart_service.repository;

import com.n11bootcamp.cart_service.entity.Cart;
import com.n11bootcamp.cart_service.entity.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID>, JpaSpecificationExecutor<Cart> {

    Optional<Cart> findByUserIdAndStatus(UUID userId, CartStatus status);

    List<Cart> findAllByUserId(UUID userId);
}