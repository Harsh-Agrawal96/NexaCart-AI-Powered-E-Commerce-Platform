package com.n11bootcamp.product_service.controller;

import com.n11bootcamp.common.response.ApiResponse;
import com.n11bootcamp.product_service.dto.request.StockRequest;
import com.n11bootcamp.product_service.dto.response.ProductResponse;
import com.n11bootcamp.product_service.dto.response.StockResponse;
import com.n11bootcamp.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
 

@RestController
@RequestMapping("/api/v1/internal/products")
@RequiredArgsConstructor
@Tag(name = "Internal - Ürünler", description = "Servisler arası stok/ürün işlemleri")
public class InternalProductController {
 
    private final ProductService productService;
 
    @PostMapping("/batch")
    @Operation(summary = "Toplu ürün getir")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByIds(
            @RequestBody List<UUID> ids) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getActiveProductsByIds(ids)));
    }

    @GetMapping("/{id}/stock/check")
    @Operation(summary = "Stok kontrolü")
    public ResponseEntity<ApiResponse<StockResponse>> checkStock(
            @PathVariable UUID id,
            @RequestParam int amount) {
        return ResponseEntity.ok(ApiResponse.ok(productService.checkStock(id, amount)));
    }
 
    @PostMapping("/{id}/stock/decrease")
    @Operation(summary = "Stok düş")
    public ResponseEntity<ApiResponse<StockResponse>> decreaseStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(productService.decreaseStock(id, request.amount())));
    }
 
    @PostMapping("/{id}/stock/increase")
    @Operation(summary = "Stok artır (iade)")
    public ResponseEntity<ApiResponse<StockResponse>> increaseStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(productService.increaseStock(id, request.amount())));
    }

    @PostMapping("/{id}/stock/reserve")
    @Operation(summary = "Stok rezerve et")
    public ResponseEntity<ApiResponse<StockResponse>> reserveStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(productService.reserveStock(id, request.amount())));
    }

    @PostMapping("/{id}/stock/release")
    @Operation(summary = "Stok rezervasyonunu geri al")
    public ResponseEntity<ApiResponse<StockResponse>> releaseReservedStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(productService.releaseReservedStock(id, request.amount())));
    }

    @PostMapping("/{id}/stock/confirm")
    @Operation(summary = "Stoku onayla")
    public ResponseEntity<ApiResponse<StockResponse>> confirmStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(productService.confirmStock(id, request.amount())));
    }
}
 
