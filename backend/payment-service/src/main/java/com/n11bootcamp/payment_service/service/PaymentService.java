package com.n11bootcamp.payment_service.service;

import java.util.UUID;

import com.n11bootcamp.payment_service.client.PaymentResult;
import com.n11bootcamp.payment_service.dto.PaymentRequestDto;

public interface PaymentService {

    PaymentResult processPayment(UUID userId, PaymentRequestDto dto);
}
