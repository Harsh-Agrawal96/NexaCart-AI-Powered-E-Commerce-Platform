package com.n11bootcamp.product_service.controller;

import com.n11bootcamp.common.response.ApiResponse;
import com.n11bootcamp.product_service.dto.request.CreateCategoryRequest;
import com.n11bootcamp.product_service.dto.request.UpdateCategoryRequest;
import com.n11bootcamp.product_service.dto.response.CategoryResponse;
import com.n11bootcamp.product_service.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
import java.util.UUID;
 
@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Admin - Kategoriler", description = "Kategori yönetimi (admin)")
public class AdminCategoryController {
 
    private final CategoryService categoryService;
 
    @PostMapping
    @Operation(summary = "Yeni kategori oluştur")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(categoryService.create(request)));
    }
 
    @PutMapping("/{id}")
    @Operation(summary = "Kategori güncelle")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.update(id, request)));
    }
 
    @GetMapping
    @Operation(summary = "Tüm kategorileri listele (silinmemiş)")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getAllForAdmin()));
    }
 
    @GetMapping("/{id}")
    @Operation(summary = "Kategori detayı")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getById(id)));
    }
 
    @DeleteMapping("/{id}")
    @Operation(summary = "Kategori sil (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}