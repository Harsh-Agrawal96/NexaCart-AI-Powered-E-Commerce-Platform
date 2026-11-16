package com.n11bootcamp.payment_service.publisher;

import com.n11bootcamp.common.constant.SagaConstants;
import com.n11bootcamp.common.event.PaymentFailedEvent;
import com.n11bootcamp.common.event.PaymentSucceededEvent;
import com.n11bootcamp.payment_service.client.PaymentRequest;
import com.n11bootcamp.payment_service.client.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishSuccess(PaymentRequest request, PaymentResult result) {
        PaymentSucceededEvent event = PaymentSucceededEvent.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .items(request.getItems())
                .build();

        rabbitTemplate.convertAndSend(
            SagaConstants.SAGA_EXCHANGE,
            SagaConstants.RK_PAYMENT_SUCCEEDED,
            event
        );

        log.info("[PAYMENT] PaymentSucceededEvent published — orderId={}, provider={}, transactionId={}",
                request.getOrderId(), request.getProviderType(), result.getTransactionId());
    }

    public void publishFail(PaymentRequest request, PaymentResult result) {
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .orderId(request.getOrderId())
                .reason(result.getFailReason())
                .items(request.getItems())
                .build();

        rabbitTemplate.convertAndSend(
            SagaConstants.SAGA_EXCHANGE,
            SagaConstants.RK_PAYMENT_FAILED,
            event
        );

        log.warn("[PAYMENT] PaymentFailedEvent published — orderId={}, provider={}, reason={}",
                request.getOrderId(), request.getProviderType(), result.getFailReason());
    }
}