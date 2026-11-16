package com.n11bootcamp.ai_service.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductAdviceRequest {

    @NotNull
    private UUID productId;

    @Size(max = 500)
    private String question;
}
