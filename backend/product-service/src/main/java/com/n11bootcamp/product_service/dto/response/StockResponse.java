package com.n11bootcamp.product_service.dto.response;

import java.util.UUID;

public record StockResponse(
        UUID productId,
        String sku,
        Integer totalQuantity,
        Integer reservedQuantity,
        Integer availableQuantity,
        boolean success
) {}
