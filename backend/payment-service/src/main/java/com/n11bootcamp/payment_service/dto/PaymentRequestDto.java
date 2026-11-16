package com.n11bootcamp.payment_service.dto;


import lombok.Getter;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import com.n11bootcamp.common.enums.PaymentProviderType;

@Getter
public class PaymentRequestDto {

    @NotNull
    private UUID orderId;

    @NotNull
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
