package com.n11bootcamp.order_service.service;

import com.n11bootcamp.common.enums.OrderStatus;
import com.n11bootcamp.common.response.PageResponse;
import com.n11bootcamp.order_service.dto.response.OrderResponse;
import com.n11bootcamp.order_service.dto.response.OrderSummaryResponse;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(UUID userId);

    void cancelOrder(UUID orderId, String reason);

    void completeOrder(UUID orderId);

    OrderResponse getOrder(UUID orderId);

    void awaitPayment(UUID orderId);

    // ─── Admin ───────────────────────────────────────────────────────────────

    PageResponse<OrderSummaryResponse> searchForAdmin(
            OrderStatus status,
            UUID userId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable);
}