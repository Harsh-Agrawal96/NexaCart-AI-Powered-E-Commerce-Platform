package com.n11bootcamp.payment_service.client.provider;

import com.iyzipay.Options;
import com.iyzipay.model.Payment;
import com.iyzipay.request.CreatePaymentRequest;
import com.n11bootcamp.common.enums.PaymentProviderType;
import com.n11bootcamp.payment_service.client.PaymentProvider;
import com.n11bootcamp.payment_service.client.PaymentRequest;
import com.n11bootcamp.payment_service.client.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IyzicoPayProvider implements PaymentProvider {

    private final Options iyzicoOptions;
    private final IyzicoRequestBuilder requestBuilder;

    @Override
    public PaymentProviderType getType() {
        return PaymentProviderType.IYZICO_PAY;
    }

    @Override
    public PaymentResult charge(PaymentRequest request) {
        log.info("[IYZICO] Initiating payment — orderId={}, amount={}",
                request.getOrderId(), request.getGrandTotal());

        try {
            CreatePaymentRequest iyzicoRequest = requestBuilder.build(request);
            Payment payment = Payment.create(iyzicoRequest, iyzicoOptions);

            if ("success".equalsIgnoreCase(payment.getStatus())) {
                log.info("[IYZICO] Payment success — orderId={}, paymentId={}",
                        request.getOrderId(), payment.getPaymentId());

                return PaymentResult.builder()
                        .success(true)
                        .transactionId(payment.getPaymentId())
                        .failReason(null)
                        .build();

            } else {
                log.warn("[IYZICO] Payment failed — orderId={}, errorMessage={}",
                        request.getOrderId(), payment.getErrorMessage());

                return PaymentResult.builder()
                        .success(false)
                        .transactionId(null)
                        .failReason(payment.getErrorMessage())
                        .build();
            }

        } catch (Exception e) {
            log.error("[IYZICO] Payment error — orderId={}, error={}",
                    request.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("Iyzico payment failed: " + e.getMessage(), e);
        }
    }
}