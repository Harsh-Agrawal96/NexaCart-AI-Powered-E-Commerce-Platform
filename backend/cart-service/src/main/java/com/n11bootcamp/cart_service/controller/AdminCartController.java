package com.n11bootcamp.cart_service.controller;

import com.n11bootcamp.cart_service.dto.response.CartResponse;
import com.n11bootcamp.cart_service.dto.response.CartSummaryResponse;
import com.n11bootcamp.cart_service.entity.enums.CartStatus;
import com.n11bootcamp.cart_service.service.CartService;
import com.n11bootcamp.common.response.ApiResponse;
import com.n11bootcamp.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@Tag(name = "Admin - Sepet", description = "Sepetleri listeleme ve detay (admin)")
@RestController
@RequestMapping("/api/v1/admin/carts")
@RequiredArgsConstructor
public class AdminCartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Sepetleri listele / filtrele",
            description = "Opsiyonel: userId, status (ACTIVE | ORDERED), createdFrom, createdTo")
    public ResponseEntity<ApiResponse<PageResponse<CartSummaryResponse>>> list(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) CartStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime createdFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime createdTo,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                cartService.searchForAdmin(userId, status, createdFrom, createdTo, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Sepet detayı", description = "Ürün adı/fiyat dahil tam sepet (product-service üzerinden)")
    public ResponseEntity<ApiResponse<CartResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCartByIdForAdmin(id)));
    }
}
