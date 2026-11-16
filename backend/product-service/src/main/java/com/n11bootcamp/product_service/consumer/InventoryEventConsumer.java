package com.n11bootcamp.product_service.consumer;

import com.n11bootcamp.common.constant.SagaConstants;
import com.n11bootcamp.common.event.OrderCreatedEvent;
import com.n11bootcamp.common.event.PaymentFailedEvent;
import com.n11bootcamp.common.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.n11bootcamp.product_service.handler.InventoryEventHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final InventoryEventHandler inventoryEventHandler;

     @RabbitListener(queues = SagaConstants.Q_INVENTORY_RESERVE, 
                    containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received order.created | orderId: {}", event.getOrderId());
        inventoryEventHandler.handleOrderCreated(event);
    }

    @RabbitListener(queues = SagaConstants.Q_INVENTORY_RELEASE, 
                    containerFactory = "rabbitListenerContainerFactory")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Received payment.failed | orderId: {}", event.getOrderId());
        inventoryEventHandler.handlePaymentFailed(event);
    }

    @RabbitListener(queues = SagaConstants.Q_INVENTORY_CONFIRM, 
                    containerFactory = "rabbitListenerContainerFactory")
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        log.info("Received payment.succeeded | orderId: {}", event.getOrderId());
        inventoryEventHandler.handlePaymentSucceeded(event);
    }
}