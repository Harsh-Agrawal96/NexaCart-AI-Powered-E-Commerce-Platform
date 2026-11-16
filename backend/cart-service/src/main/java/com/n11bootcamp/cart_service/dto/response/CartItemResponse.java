package com.n11bootcamp.cart_service.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID productId,
        String productName,
        String productSku,
        BigDecimal unitPrice,
        Integer amount,
        BigDecimal totalPrice
) {}