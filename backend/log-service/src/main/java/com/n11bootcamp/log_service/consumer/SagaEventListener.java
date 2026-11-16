package com.n11bootcamp.log_service.consumer;

import java.util.UUID;

import com.n11bootcamp.common.event.*;
import com.n11bootcamp.log_service.config.RabbitMQConfig;
import com.n11bootcamp.log_service.entity.enums.EventType;
import com.n11bootcamp.log_service.service.SagaLogEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaEventListener {

    private final SagaLogEntryService sagaLogEntryService;

    @RabbitListener(queues = RabbitMQConfig.Q_LOG_ORDER_CREATED)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("[LISTENER] OrderCreatedEvent received: orderId={}", event.getOrderId());
        persist(EventType.ORDER_CREATED, "order-service",
                "Order created with total: " + event.getGrandTotal(),
                event.getOrderId(), event.getUserId(), event);
    }

    @RabbitListener(queues = RabbitMQConfig.Q_LOG_INVENTORY_RESERVED)
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("[LISTENER] InventoryReservedEvent received: orderId={}", event.getOrderId());
        persist(EventType.INVENTORY_RESERVED, "product-service",
                "Inventory reserved for order: " + event.getOrderId(),
                event.getOrderId(), event.getUserId(), event);
    }

    @RabbitListener(queues = RabbitMQConfig.Q_LOG_INVENTORY_FAILED)
    public void handleInventoryFailed(InventoryFailedEvent event) {
        log.info("[LISTENER] InventoryFailedEvent received: orderId={}", event.getOrderId());
        persist(EventType.INVENTORY_FAILED, "product-service",
                "Inventory reservation failed: " + event.getReason(),
                event.getOrderId(), null, event);
    }

    @RabbitListener(queues = RabbitMQConfig.Q_LOG_PAYMENT_SUCCEEDED)
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        log.info("[LISTENER] PaymentSucceededEvent received: orderId={}", event.getOrderId());
        persist(EventType.PAYMENT_SUCCEEDED, "payment-service",
                "Payment succeeded for order: " + event.getOrderId(),
                event.getOrderId(), event.getUserId(), event);
    }

    @RabbitListener(queues = RabbitMQConfig.Q_LOG_PAYMENT_FAILED)
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("[LISTENER] PaymentFailedEvent received: orderId={}", event.getOrderId());
        persist(EventType.PAYMENT_FAILED, "payment-service",
                "Payment failed: " + event.getReason(),
                event.getOrderId(), null, event);
    }

    private void persist(EventType eventType,
                         String serviceName,
                         String message,
                         UUID orderId,
                         UUID userId,
                         Object payload) {
        sagaLogEntryService.saveSagaEvent(
                eventType,
                serviceName,
                message,
                orderId != null ? orderId.toString() : null,
                userId != null ? userId.toString() : null,
                payload);
    }
}