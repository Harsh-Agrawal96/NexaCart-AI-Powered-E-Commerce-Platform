package com.n11bootcamp.payment_service.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.n11bootcamp.common.enums.OrderStatus;
import com.n11bootcamp.payment_service.client.PaymentProvider;
import com.n11bootcamp.payment_service.client.PaymentProviderFactory;
import com.n11bootcamp.payment_service.client.PaymentRequest;
import com.n11bootcamp.payment_service.client.PaymentResult;
import com.n11bootcamp.payment_service.client.feign.OrderClient;
import com.n11bootcamp.payment_service.dto.OrderResponse;
import com.n11bootcamp.payment_service.dto.PaymentRequestDto;
import com.n11bootcamp.payment_service.mapper.PaymentRequestMapper;
import com.n11bootcamp.payment_service.publisher.PaymentEventPublisher;
import com.n11bootcamp.payment_service.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentProviderFactory providerFactory;
    private final PaymentRequestMapper requestMapper;
    private final PaymentEventPublisher eventPublisher;
    private final OrderClient orderClient;

    @Override
    public PaymentResult processPayment(UUID userId, PaymentRequestDto dto) {
        OrderResponse order = orderClient.getOrder(dto.getOrderId());
        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            return PaymentResult.builder()
                    .success(false)
                    .failReason("Sipariş ödeme bekliyor durumunda değil. Mevcut durum: " + order.getStatus())
                    .build();
        }

        PaymentRequest request = requestMapper.toPaymentRequest(userId, dto, order);

        PaymentProvider provider = providerFactory.getProvider(request.getProviderType());
        PaymentResult result = provider.charge(request);
        if (result.isSuccess()) {
            eventPublisher.publishSuccess(request, result);
        } else {
            eventPublisher.publishFail(request, result);
        }
        return result;
    }
}
