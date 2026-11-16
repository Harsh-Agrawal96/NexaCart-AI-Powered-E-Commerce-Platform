package com.n11bootcamp.product_service.handler;

import com.n11bootcamp.common.constant.SagaConstants;
import com.n11bootcamp.common.event.*;
import com.n11bootcamp.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventHandler {

    private final ProductService productService;
    private final RabbitTemplate rabbitTemplate;

    public void handleOrderCreated(OrderCreatedEvent event) {
        if (event.getItems() == null || event.getItems().isEmpty()) {
            throw new IllegalArgumentException("OrderCreatedEvent.items must not be null/empty");
        }

        List<OrderItemPayload> reserved = new ArrayList<>();
        try {
            for (OrderItemPayload item : event.getItems()) {
                productService.reserveStock(item.getProductId(), item.getAmount());
                reserved.add(item);
            }

            rabbitTemplate.convertAndSend(
                    SagaConstants.SAGA_EXCHANGE,
                    SagaConstants.RK_INVENTORY_RESERVED,
                    InventoryReservedEvent.builder()
                            .orderId(event.getOrderId())
                            .userId(event.getUserId())
                            .items(event.getItems())
                            .grandTotal(event.getGrandTotal())
                            .build()
            );
            log.info("inventory.reserved published | orderId: {}, items: {}", event.getOrderId(), event.getItems().size());
            return;

        } catch (Exception e) {
            rollbackReserved(event.getOrderId(), reserved);

            rabbitTemplate.convertAndSend(
                    SagaConstants.SAGA_EXCHANGE,
                    SagaConstants.RK_INVENTORY_FAILED,
                    InventoryFailedEvent.builder()
                            .orderId(event.getOrderId())
                            .reason(e.getMessage())
                            .build()
            );
            log.info("inventory.failed published | orderId: {}", event.getOrderId());
            return;
        }
    }

    private void rollbackReserved(UUID orderId, List<OrderItemPayload> reserved) {
        for (int i = reserved.size() - 1; i >= 0; i--) {
            OrderItemPayload item = reserved.get(i);
            try {
                productService.releaseReservedStock(item.getProductId(), item.getAmount());
            } catch (Exception rollbackEx) {
                log.error("Rollback reserve failed | orderId: {}, productId: {}, reason: {}",
                        orderId, item.getProductId(), rollbackEx.getMessage());
            }
        }
    }

    public void handlePaymentFailed(PaymentFailedEvent event) {
        if (event.getItems() == null || event.getItems().isEmpty()) {
            throw new IllegalArgumentException("PaymentFailedEvent.items must not be null/empty");
        }

        for (OrderItemPayload item : event.getItems()) {
            try {
                productService.releaseReservedStock(item.getProductId(), item.getAmount());
            } catch (Exception e) {
                log.error("Release failed | orderId: {}, productId: {}, reason: {}",
                        event.getOrderId(), item.getProductId(), e.getMessage());
            }
        }
    }

    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        if (event.getItems() == null || event.getItems().isEmpty()) {
            throw new IllegalArgumentException("PaymentSucceededEvent.items must not be null/empty");
        }

        for (OrderItemPayload item : event.getItems()) {
            try {
                productService.confirmStock(item.getProductId(), item.getAmount());
            } catch (Exception e) {
                log.error("Confirm failed | orderId: {}, productId: {}, reason: {}",
                        event.getOrderId(), item.getProductId(), e.getMessage());
                throw e;
            }
        }
    }
}