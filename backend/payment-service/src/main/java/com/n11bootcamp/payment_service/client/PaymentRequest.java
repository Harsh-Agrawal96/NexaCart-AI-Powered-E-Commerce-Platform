package com.n11bootcamp.payment_service.client;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.n11bootcamp.common.enums.PaymentProviderType;
import com.n11bootcamp.common.event.OrderItemPayload;

@Getter
@Builder
public class PaymentRequest {
    private UUID orderId;
    private UUID userId;
    private BigDecimal grandTotal;
    private List<OrderItemPayload> items;
    private PaymentProviderType providerType;

    private String cardHolderName;
    private String cardNumber;
    private String expireMonth;
    private String expireYear;
    private String cvc;

    private String buyerName;
    private String buyerSurname;
    private String buyerEmail;
    private String buyerPhone;
    private String buyerCity;
    private String buyerAddress;
}