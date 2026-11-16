package com.n11bootcamp.product_service.controller;

import com.n11bootcamp.common.response.ApiResponse;
import com.n11bootcamp.product_service.dto.request.CreateProductRequest;
import com.n11bootcamp.product_service.dto.request.UpdateProductRequest;
import com.n11bootcamp.product_service.dto.response.ProductResponse;
import com.n11bootcamp.product_service.dto.response.ProductSummaryResponse;
import com.n11bootcamp.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.UUID;
 
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin - Ürünler", description = "Ürün yönetimi (admin)")
public class AdminProductController {
 
    private final ProductService productService;
 
    @PostMapping
    @Operation(summary = "Yeni ürün oluştur")
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(productService.create(request)));
    }
 
    @PutMapping("/{id}")
    @Operation(summary = "Ürün güncelle")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(productService.update(id, request)));
    }
 
    @GetMapping
    @Operation(summary = "Tüm ürünleri listele (aktif + pasif, silinmemiş)")
    public ResponseEntity<ApiResponse<Page<ProductSummaryResponse>>> getAll(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
 
        Page<ProductSummaryResponse> page =
                productService.getAllForAdmin(categoryId, search, pageable);
 
        return ResponseEntity.ok(ApiResponse.ok(page));
    }
 
    @GetMapping("/{id}")
    @Operation(summary = "Ürün detayı")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getById(id, null)));
    }
 
    @DeleteMapping("/{id}")
    @Operation(summary = "Ürün sil (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
