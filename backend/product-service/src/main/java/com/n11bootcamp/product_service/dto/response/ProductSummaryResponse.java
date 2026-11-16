package com.n11bootcamp.product_service.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSummaryResponse(
        UUID id,
        String name,
        BigDecimal price,
        Integer quantity,
        String sku,
        String imageUrl,
        boolean active,
        UUID categoryId,
        String categoryName
) {}