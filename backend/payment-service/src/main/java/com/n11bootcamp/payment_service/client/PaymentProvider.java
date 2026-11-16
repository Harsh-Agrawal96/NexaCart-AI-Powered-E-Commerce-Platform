package com.n11bootcamp.payment_service.client;

import com.n11bootcamp.common.enums.PaymentProviderType;

public interface PaymentProvider {
    PaymentProviderType getType();
    PaymentResult charge(PaymentRequest request);
}