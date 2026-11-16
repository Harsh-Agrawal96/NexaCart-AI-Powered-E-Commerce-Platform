package com.n11bootcamp.cart_service.controller;

import com.n11bootcamp.cart_service.dto.request.AddItemRequest;
import com.n11bootcamp.cart_service.dto.request.UpdateItemRequest;
import com.n11bootcamp.cart_service.dto.response.CartResponse;
import com.n11bootcamp.cart_service.service.CartService;
import com.n11bootcamp.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Sepet", description = "Sepet işlemleri")
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    @Operation(summary = "Sepete ürün ekle")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody AddItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.addItem(userId, request)));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Sepetten ürün çıkar")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID productId) {
        cartService.removeItem(userId, productId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping
    @Operation(summary = "Aktif sepeti getir")
    public ResponseEntity<ApiResponse<CartResponse>> getActiveCart(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.getActiveCart(userId)));
    }

    @PatchMapping("/items/{productId}")
    @Operation(summary = "Sepetteki ürün miktarını güncelle")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemAmount(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                cartService.updateItemAmount(userId, productId, request.amount())));
    }
}