package com.n11bootcamp.order_service.controller;

import com.n11bootcamp.common.response.ApiResponse;
import com.n11bootcamp.order_service.dto.response.OrderResponse;
import com.n11bootcamp.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Tag(name = "Siparişler", description = "Sipariş işlemleri")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Sipariş oluştur")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        log.info("POST /api/v1/orders - userId: {}", userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(orderService.createOrder(userId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Sipariş detayı getir")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable UUID id) {
        log.info("GET /api/v1/orders/{}", id);
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrder(id)));
    }
}
