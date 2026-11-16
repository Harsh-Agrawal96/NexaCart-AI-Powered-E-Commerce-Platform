package com.n11bootcamp.order_service.consumer;

import com.n11bootcamp.common.constant.SagaConstants;
import com.n11bootcamp.common.event.InventoryFailedEvent;
import com.n11bootcamp.common.event.InventoryReservedEvent;
import com.n11bootcamp.common.event.PaymentFailedEvent;
import com.n11bootcamp.common.event.PaymentSucceededEvent;
import com.n11bootcamp.order_service.handler.OrderSagaEventHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderSagaEventHandler orderSagaEventHandler;

    @RabbitListener(
            queues = SagaConstants.Q_ORDER_CANCEL_INVENTORY,
            containerFactory = "rabbitListenerContainerFactory")
    public void onInventoryFailed(InventoryFailedEvent event) {
        orderSagaEventHandler.handleInventoryFailed(event);
    }

    @RabbitListener(
            queues = SagaConstants.Q_ORDER_CANCEL_PAYMENT,
            containerFactory = "rabbitListenerContainerFactory")
    public void onPaymentFailed(PaymentFailedEvent event) {
        orderSagaEventHandler.handlePaymentFailed(event);
    }

    @RabbitListener(
            queues = SagaConstants.Q_ORDER_COMPLETE,
            containerFactory = "rabbitListenerContainerFactory")
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        orderSagaEventHandler.handlePaymentSucceeded(event);
    }

    @RabbitListener(
            queues = SagaConstants.Q_ORDER_AWAIT_PAYMENT,
            containerFactory = "rabbitListenerContainerFactory")
    public void onInventoryReserved(InventoryReservedEvent event) {
        orderSagaEventHandler.handleInventoryReserved(event);
    }
}
