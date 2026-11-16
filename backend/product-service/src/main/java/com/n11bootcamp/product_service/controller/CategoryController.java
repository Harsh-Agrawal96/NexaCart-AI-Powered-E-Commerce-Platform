package com.n11bootcamp.product_service.controller;

import com.n11bootcamp.common.response.ApiResponse;
import com.n11bootcamp.product_service.dto.response.CategoryResponse;
import com.n11bootcamp.product_service.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import java.util.List;
import java.util.UUID;
 
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Kategoriler", description = "Kategori listeleme işlemleri")
public class CategoryController {
 
    private final CategoryService categoryService;
 
    @GetMapping
    @Operation(summary = "Aktif kategorileri listele")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllActive() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getAllActive()));
    }
 
    @GetMapping("/{id}")
    @Operation(summary = "Kategori detayı")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getById(id)));
    }
}