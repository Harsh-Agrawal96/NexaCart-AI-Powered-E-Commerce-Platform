package com.n11bootcamp.cart_service.consumer;

import com.n11bootcamp.cart_service.handler.CartPaymentEventHandler;
import com.n11bootcamp.common.constant.SagaConstants;
import com.n11bootcamp.common.event.PaymentSucceededEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartEventConsumer {

    private final CartPaymentEventHandler cartPaymentEventHandler;

    @RabbitListener(
            queues = SagaConstants.Q_CART_CLEAR,
            containerFactory = "rabbitListenerContainerFactory")
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        log.debug("Queue {} — PaymentSucceededEvent received", SagaConstants.Q_CART_CLEAR);
        cartPaymentEventHandler.handlePaymentSucceeded(event);
    }
}
