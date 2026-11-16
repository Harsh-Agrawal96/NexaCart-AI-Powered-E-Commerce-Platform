package com.n11bootcamp.cart_service.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String sku,
        BigDecimal price,
        boolean active,
        int availableQuantity
) {}
