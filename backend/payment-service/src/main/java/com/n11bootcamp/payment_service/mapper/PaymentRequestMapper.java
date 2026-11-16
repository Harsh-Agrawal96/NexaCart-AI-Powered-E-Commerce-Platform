package com.n11bootcamp.payment_service.mapper;

import com.n11bootcamp.payment_service.client.PaymentRequest;
import com.n11bootcamp.payment_service.dto.OrderResponse;
import com.n11bootcamp.payment_service.dto.PaymentRequestDto;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentRequestMapper {

    public PaymentRequest toPaymentRequest(UUID userId, PaymentRequestDto dto, OrderResponse order) {
        return PaymentRequest.builder()
                .orderId(dto.getOrderId())
                .userId(userId)
                .grandTotal(order.getGrandTotal())
                .items(order.getItems())
                .providerType(dto.getProviderType())
                // kart
                .cardHolderName(dto.getCardHolderName())
                .cardNumber(dto.getCardNumber())
                .expireMonth(dto.getExpireMonth())
                .expireYear(dto.getExpireYear())
                .cvc(dto.getCvc())
                // buyer
                .buyerName(dto.getBuyerName())
                .buyerSurname(dto.getBuyerSurname())
                .buyerEmail(dto.getBuyerEmail())
                .buyerPhone(dto.getBuyerPhone())
                .buyerCity(dto.getBuyerCity())
                .buyerAddress(dto.getBuyerAddress())
                .build();
    }
}