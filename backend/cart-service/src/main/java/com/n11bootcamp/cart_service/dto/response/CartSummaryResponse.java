package com.n11bootcamp.cart_service.dto.response;

import com.n11bootcamp.cart_service.entity.enums.CartStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CartSummaryResponse(
        UUID cartId,
        UUID userId,
        CartStatus status,
        int lineItemCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
