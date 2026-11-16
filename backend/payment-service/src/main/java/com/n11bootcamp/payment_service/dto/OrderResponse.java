package com.n11bootcamp.payment_service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.n11bootcamp.common.enums.OrderStatus;
import com.n11bootcamp.common.event.OrderItemPayload;

@Getter
@NoArgsConstructor
public class OrderResponse {
    private UUID id;
    private UUID userId;
    private BigDecimal grandTotal;
    private List<OrderItemPayload> items;
    private OrderStatus status;
    private String cancelReason;
}