package com.n11bootcamp.cart_service.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID cartId,
        UUID userId,
        List<CartItemResponse> items,
        BigDecimal grandTotal
) {}
