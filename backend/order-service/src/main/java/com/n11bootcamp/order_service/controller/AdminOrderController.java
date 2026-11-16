package com.n11bootcamp.order_service.controller;

import com.n11bootcamp.common.enums.OrderStatus;
import com.n11bootcamp.common.response.ApiResponse;
import com.n11bootcamp.common.response.PageResponse;
import com.n11bootcamp.order_service.dto.response.OrderResponse;
import com.n11bootcamp.order_service.dto.response.OrderSummaryResponse;
import com.n11bootcamp.order_service.service.OrderService;
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

@Tag(name = "Admin - Siparişler", description = "Sipariş yönetimi ve sorgulama (admin)")
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Siparişleri listele / filtrele",
            description = "Opsiyonel: status, userId, oluşturulma tarihi aralığı (createdFrom, createdTo)")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> search(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime createdFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime createdTo,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.ok(
                orderService.searchForAdmin(status, userId, createdFrom, createdTo, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Sipariş detayı")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrder(id)));
    }
}
