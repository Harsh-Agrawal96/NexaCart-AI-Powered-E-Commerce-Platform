package com.n11bootcamp.order_service.mapper;

import com.n11bootcamp.common.enums.OrderStatus;
import com.n11bootcamp.common.event.OrderCreatedEvent;
import com.n11bootcamp.common.event.OrderItemPayload;
import com.n11bootcamp.order_service.dto.response.CartResponse;
import com.n11bootcamp.order_service.dto.response.CartItemResponse;
import com.n11bootcamp.order_service.dto.response.OrderItemResponse;
import com.n11bootcamp.order_service.dto.response.OrderResponse;
import com.n11bootcamp.order_service.dto.response.OrderSummaryResponse;
import com.n11bootcamp.order_service.entity.Order;
import com.n11bootcamp.order_service.entity.OrderItem;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class OrderMapper {

    public Order toEntity(UUID userId, CartResponse cart) {
        Order order = Order.builder()
                .userId(userId)
                .grandTotal(cart.getGrandTotal())
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItem> items = cart.getItems().stream()
                .map(i -> toOrderItem(i, order))
                .toList();
        order.setItems(items);
        return order;
    }

    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .grandTotal(order.getGrandTotal())
                .items(order.getItems().stream().map(this::toOrderItemResponse).toList())
                .status(order.getStatus())
                .cancelReason(order.getCancelReason())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public OrderSummaryResponse toSummaryResponse(Order order) {
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .grandTotal(order.getGrandTotal())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public OrderCreatedEvent toOrderCreatedEvent(Order order) {
        return OrderCreatedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .grandTotal(order.getGrandTotal())
                .items(order.getItems().stream().map(this::toOrderItemPayload).toList())
                .build();
    }

    private OrderItem toOrderItem(CartItemResponse item, Order order) {
        BigDecimal totalPrice = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getAmount()));

        return OrderItem.builder()
                .order(order)
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .amount(item.getAmount())
                .unitPrice(item.getUnitPrice())
                .totalPrice(totalPrice)
                .build();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .amount(item.getAmount())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    private OrderItemPayload toOrderItemPayload(OrderItem item) {
        return OrderItemPayload.builder()
                .productId(item.getProductId())
                .amount(item.getAmount())
                .unitPrice(item.getUnitPrice())
                .build();
    }
}