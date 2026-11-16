package com.n11bootcamp.product_service.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(
 
        @Size(max = 200, message = "Ürün adı en fazla 200 karakter olabilir")
        String name,
 
        String description,
 
        @DecimalMin(value = "0.0", inclusive = false, message = "Fiyat sıfırdan büyük olmalıdır")
        @Digits(integer = 8, fraction = 2, message = "Fiyat en fazla 8 tam 2 ondalık basamak içerebilir")
        BigDecimal price,
 
        @Min(value = 0, message = "Stok miktarı negatif olamaz")
        Integer quantity,
 
        String imageUrl,
 
        UUID categoryId,
 
        Boolean active
) {}
 