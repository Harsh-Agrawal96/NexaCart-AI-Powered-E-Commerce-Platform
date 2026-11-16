package com.n11bootcamp.ai_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class ReviewResponse {

    private UUID id;
    private UUID productId;
    private UUID userId;
    private String userFirstName;
    private String userLastName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
