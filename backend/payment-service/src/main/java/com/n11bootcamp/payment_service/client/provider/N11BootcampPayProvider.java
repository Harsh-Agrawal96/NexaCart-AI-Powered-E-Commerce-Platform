package com.n11bootcamp.payment_service.client.provider;

import com.n11bootcamp.common.enums.PaymentProviderType;
import com.n11bootcamp.payment_service.client.PaymentProvider;
import com.n11bootcamp.payment_service.client.PaymentRequest;
import com.n11bootcamp.payment_service.client.PaymentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class N11BootcampPayProvider implements PaymentProvider {

    @Override
    public PaymentProviderType getType() {
        return PaymentProviderType.N11_BOOTCAMP_PAY;
    }

    @Override
    public PaymentResult charge(PaymentRequest request) {
        String transactionId = UUID.randomUUID().toString();
        log.info("[N11BOOTCAMP] Payment success — orderId={}, amount={}, transactionId={}",
                request.getOrderId(), request.getGrandTotal(), transactionId);

        return PaymentResult.builder()
                .success(true)
                .transactionId(transactionId)
                .failReason(null)
                .build();
    }
}
