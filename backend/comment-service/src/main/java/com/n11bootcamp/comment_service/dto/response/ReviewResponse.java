package com.n11bootcamp.comment_service.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID productId,
        UUID userId,
        String userFirstName,
        String userLastName,
        Integer rating,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

