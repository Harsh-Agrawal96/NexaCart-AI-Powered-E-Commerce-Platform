package com.n11bootcamp.comment_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateReviewRequest(

        @NotNull(message = "productId boş olamaz")
        UUID productId,

        @NotNull(message = "rating boş olamaz")
        @Min(value = 1, message = "rating en az 1 olmalıdır")
        @Max(value = 5, message = "rating en fazla 5 olmalıdır")
        Integer rating,

        @Size(max = 2000, message = "Yorum en fazla 2000 karakter olabilir")
        String comment
) {}

