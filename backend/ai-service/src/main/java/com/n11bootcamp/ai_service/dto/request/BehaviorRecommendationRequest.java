package com.n11bootcamp.ai_service.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BehaviorRecommendationRequest {

    @NotNull
    private UUID userId;

    /** Ürün detay bağlamı; opsiyonel. */
    private UUID currentProductId;

    @Min(1)
    @Max(100)
    private int limit = 10;
}
