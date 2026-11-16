package com.n11bootcamp.cart_service.dto.request;

import jakarta.validation.constraints.Min;

public record UpdateItemRequest(
    @Min(1) Integer amount
) {}
