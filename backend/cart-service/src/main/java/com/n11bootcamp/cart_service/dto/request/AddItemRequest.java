package com.n11bootcamp.cart_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddItemRequest(

        @NotNull(message = "Product ID cannot be null")
        UUID productId,

        @NotNull(message = "Amount cannot be null")
        @Min(value = 1, message = "Amount must be greater than 0")
        Integer amount
) {}
