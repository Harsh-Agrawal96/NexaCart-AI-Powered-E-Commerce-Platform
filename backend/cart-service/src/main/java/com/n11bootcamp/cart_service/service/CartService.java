package com.n11bootcamp.cart_service.service;

import com.n11bootcamp.cart_service.dto.request.AddItemRequest;
import com.n11bootcamp.cart_service.dto.response.CartResponse;
import com.n11bootcamp.cart_service.dto.response.CartSummaryResponse;
import com.n11bootcamp.cart_service.entity.enums.CartStatus;

import com.n11bootcamp.common.response.PageResponse;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CartService {

    CartResponse addItem(UUID userId, AddItemRequest request);

    void removeItem(UUID userId, UUID productId);

    CartResponse getActiveCart(UUID userId);

    void clearCart(UUID userId);

    CartResponse updateItemAmount(UUID userId, UUID productId, Integer amount);

    // ─── Admin ───────────────────────────────────────────────────────────────

    PageResponse<CartSummaryResponse> searchForAdmin(
            UUID userId,
            CartStatus status,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable);

    CartResponse getCartByIdForAdmin(UUID cartId);
}