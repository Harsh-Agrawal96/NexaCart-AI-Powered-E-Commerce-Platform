package com.n11bootcamp.payment_service.client.provider;

import com.n11bootcamp.common.enums.PaymentProviderType;
import com.n11bootcamp.payment_service.client.PaymentProvider;
import com.n11bootcamp.payment_service.client.PaymentRequest;
import com.n11bootcamp.payment_service.client.PaymentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AlwaysFailPayProvider implements PaymentProvider {

    @Override
    public PaymentProviderType getType() {
        return PaymentProviderType.ALWAYS_FAIL_PAY;
    }

    @Override
    public PaymentResult charge(PaymentRequest request) {
        log.warn("[ALWAYS_FAIL] Payment failed intentionally — orderId={}", request.getOrderId());

        return PaymentResult.builder()
                .success(false)
                .transactionId(null)
                .failReason("Payment intentionally failed for testing purposes.")
                .build();
    }
}
