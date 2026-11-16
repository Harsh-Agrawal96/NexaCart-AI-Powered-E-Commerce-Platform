package com.n11bootcamp.order_service.controller;

import com.n11bootcamp.order_service.dto.response.OrderResponse;
import com.n11bootcamp.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Tag(name = "Internal - Siparişler", description = "Servisler arası sipariş sorguları")
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class OrderInternalController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    @Operation(summary = "Sipariş getir")
    public OrderResponse getOrder(@PathVariable UUID id) {
        log.info("INTERNAL GET /internal/orders/{}", id);
        return orderService.getOrder(id);
    }
}
