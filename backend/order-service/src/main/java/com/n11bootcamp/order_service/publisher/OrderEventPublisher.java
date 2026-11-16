package com.n11bootcamp.order_service.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.n11bootcamp.common.constant.SagaConstants;
import com.n11bootcamp.common.event.OrderCreatedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent for orderId: {}", event.getOrderId());
        rabbitTemplate.convertAndSend(
                SagaConstants.SAGA_EXCHANGE,
                SagaConstants.RK_ORDER_CREATED,
                event
        );
        log.info("OrderCreatedEvent published successfully for orderId: {}", event.getOrderId());
    }
}