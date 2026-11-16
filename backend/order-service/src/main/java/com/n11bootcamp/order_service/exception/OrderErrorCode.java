package com.n11bootcamp.order_service.exception;

import com.n11bootcamp.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements BaseErrorCode {

    ORDER_NOT_FOUND("ORD-404", "order.not.found", "order.not.found.title", HttpStatus.NOT_FOUND),
    ORDER_INVALID_AMOUNT("ORD-400", "order.invalid.amount", "order.invalid.amount.title", HttpStatus.BAD_REQUEST),
    ORDER_INVALID_PRICE("ORD-400", "order.invalid.price", "order.invalid.price.title", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_CANCELLED("ORD-409", "order.already.cancelled", "order.already.cancelled.title", HttpStatus.CONFLICT),
    ORDER_ALREADY_COMPLETED("ORD-409", "order.already.completed", "order.already.completed.title", HttpStatus.CONFLICT),
    INVALID_ORDER_STATUS("ORD-409", "order.invalid.status", "order.invalid.status.title", HttpStatus.CONFLICT),

    CART_NOT_FOUND("ORD-404-2", "order.cart.not.found", "order.cart.not.found.title", HttpStatus.NOT_FOUND),
    CART_EMPTY("ORD-400-2", "order.cart.empty", "order.cart.empty.title", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String messageKey;
    private final String titleKey;
    private final HttpStatus httpStatus;
}