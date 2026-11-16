package com.n11bootcamp.payment_service.exception;

import org.springframework.http.HttpStatus;

import com.n11bootcamp.common.exception.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements BaseErrorCode {

    UNKNOWN_PAYMENT_PROVIDER(
            "PAY-001",
            "payment.provider.unknown",
            "payment.provider.unknown.title",
            HttpStatus.BAD_REQUEST);

    private final String code;
    private final String messageKey;
    private final String titleKey;
    private final HttpStatus httpStatus;
}
