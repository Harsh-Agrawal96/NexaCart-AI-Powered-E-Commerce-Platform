package com.n11bootcamp.payment_service.client;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentResult {
    private boolean success;
    private String transactionId;
    private String failReason;
}
