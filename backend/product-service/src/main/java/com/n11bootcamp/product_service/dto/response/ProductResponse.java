package com.n11bootcamp.product_service.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        Integer quantity,
        Integer availableQuantity,
        String sku,
        String imageUrl,
        boolean active,
        CategoryResponse category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}