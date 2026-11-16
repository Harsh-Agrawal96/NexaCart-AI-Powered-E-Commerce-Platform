package com.n11bootcamp.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.n11bootcamp.order_service.dto.response.CartResponse;

import java.util.UUID;

@FeignClient(name = "cart-service", url = "${services.cart.url}")
public interface CartServiceClient {

    @GetMapping("/internal/cart/{userId}/active")
    CartResponse getActiveCart(@PathVariable UUID userId);
}