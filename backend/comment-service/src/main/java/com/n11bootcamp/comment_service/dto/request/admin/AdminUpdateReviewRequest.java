package com.n11bootcamp.comment_service.dto.request.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record AdminUpdateReviewRequest(

        @Min(value = 1, message = "rating en az 1 olmalıdır")
        @Max(value = 5, message = "rating en fazla 5 olmalıdır")
        Integer rating,

        @Size(max = 2000, message = "Yorum en fazla 2000 karakter olabilir")
        String comment
) {}

