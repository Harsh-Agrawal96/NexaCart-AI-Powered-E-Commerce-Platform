package com.n11bootcamp.product_service.controller;

import com.n11bootcamp.common.constant.HeaderConstants;
import com.n11bootcamp.common.response.ApiResponse;
import com.n11bootcamp.product_service.dto.response.ProductResponse;
import com.n11bootcamp.product_service.dto.response.ProductSummaryResponse;
import com.n11bootcamp.product_service.service.ProductService;
import com.n11bootcamp.product_service.web.UserIdHeaderResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.math.BigDecimal;
import java.util.UUID;
 
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Ürünler", description = "Ürün listeleme işlemleri")
public class ProductController {
 
    private final ProductService productService;
    private final UserIdHeaderResolver userIdHeaderResolver;
 
    @GetMapping
    @Operation(summary = "Aktif ürünleri listele")
    public ResponseEntity<ApiResponse<Page<ProductSummaryResponse>>> getAll(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
 
        Page<ProductSummaryResponse> page =
                productService.getAllActive(categoryId, search, minPrice, maxPrice, pageable);
 
        return ResponseEntity.ok(ApiResponse.ok(page));
    }
 
    @GetMapping("/{id}")
    @Operation(summary = "Ürün detayı")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(
            @PathVariable UUID id,
            @RequestHeader(value = HeaderConstants.USER_ID, required = false) String userIdHeader) {
        return ResponseEntity.ok(ApiResponse.ok(
                productService.getById(id, userIdHeaderResolver.resolve(userIdHeader))));
    }
}