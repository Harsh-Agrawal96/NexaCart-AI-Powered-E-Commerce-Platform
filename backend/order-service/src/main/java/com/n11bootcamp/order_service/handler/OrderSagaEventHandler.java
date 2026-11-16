package com.n11bootcamp.order_service.handler;

import com.n11bootcamp.common.event.InventoryFailedEvent;
import com.n11bootcamp.common.event.InventoryReservedEvent;
import com.n11bootcamp.common.event.PaymentFailedEvent;
import com.n11bootcamp.common.event.PaymentSucceededEvent;
import com.n11bootcamp.order_service.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaEventHandler {

    private final OrderService orderService;

    public void handleInventoryFailed(InventoryFailedEvent event) {
        log.info("InventoryFailedEvent orderId={}, reason={}", event.getOrderId(), event.getReason());
        orderService.cancelOrder(event.getOrderId(), event.getReason());
    }

    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("PaymentFailedEvent orderId={}, reason={}", event.getOrderId(), event.getReason());
        orderService.cancelOrder(event.getOrderId(), event.getReason());
    }

    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        log.info("PaymentSucceededEvent orderId={}", event.getOrderId());
        orderService.completeOrder(event.getOrderId());
    }

    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("InventoryReservedEvent orderId={}", event.getOrderId());
        orderService.awaitPayment(event.getOrderId());
    }
}
