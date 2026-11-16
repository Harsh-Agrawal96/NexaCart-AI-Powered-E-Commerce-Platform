package com.n11bootcamp.cart_service.controller;

import com.n11bootcamp.cart_service.dto.response.CartResponse;
import com.n11bootcamp.cart_service.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Internal - Sepet", description = "Servisler arası sepet sorguları")
@RestController
@RequestMapping("/internal/cart")
@RequiredArgsConstructor
public class InternalCartController {

    private final CartService cartService;

    @GetMapping("/{userId}/active")
    @Operation(summary = "Kullanıcının aktif sepetini getir")
    public CartResponse getActiveCart(@PathVariable UUID userId) {
        return cartService.getActiveCart(userId);
    }
}