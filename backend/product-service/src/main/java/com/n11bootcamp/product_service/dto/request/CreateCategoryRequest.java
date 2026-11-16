package com.n11bootcamp.product_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
 
        @NotBlank(message = "Kategori adı boş olamaz")
        @Size(max = 100, message = "Kategori adı en fazla 100 karakter olabilir")
        String name,
 
        @NotBlank(message = "Slug boş olamaz")
        @Size(max = 120, message = "Slug en fazla 120 karakter olabilir")
        String slug,
 
        @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir")
        String description,
 
        String imageUrl,
 
        Integer displayOrder
) {}