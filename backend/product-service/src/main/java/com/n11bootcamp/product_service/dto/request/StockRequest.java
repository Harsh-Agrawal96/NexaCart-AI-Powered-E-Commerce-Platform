package com.n11bootcamp.product_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockRequest(
 
        @NotNull(message = "Miktar boş olamaz")
        @Min(value = 1, message = "Miktar en az 1 olmalıdır")
        Integer amount
) {}