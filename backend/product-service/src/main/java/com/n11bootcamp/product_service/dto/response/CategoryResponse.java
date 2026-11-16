package com.n11bootcamp.product_service.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String imageUrl,
        Integer displayOrder,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}