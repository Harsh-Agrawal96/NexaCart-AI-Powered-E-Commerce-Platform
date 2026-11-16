package com.n11bootcamp.product_service.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequest(
 
        @NotBlank(message = "Ürün adı boş olamaz")
        @Size(max = 200, message = "Ürün adı en fazla 200 karakter olabilir")
        String name,
 
        String description,
 
        @NotNull(message = "Fiyat boş olamaz")
        @DecimalMin(value = "0.0", inclusive = false, message = "Fiyat sıfırdan büyük olmalıdır")
        @Digits(integer = 8, fraction = 2, message = "Fiyat en fazla 8 tam 2 ondalık basamak içerebilir")
        BigDecimal price,
 
        @NotNull(message = "Stok miktarı boş olamaz")
        @Min(value = 0, message = "Stok miktarı negatif olamaz")
        Integer quantity,
 
        @NotBlank(message = "SKU boş olamaz")
        @Size(max = 100, message = "SKU en fazla 100 karakter olabilir")
        String sku,
 
        String imageUrl,
 
        @NotNull(message = "Kategori seçilmesi zorunludur")
        UUID categoryId
) {}
