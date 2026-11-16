package com.n11bootcamp.cart_service.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import com.n11bootcamp.common.exception.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode implements BaseErrorCode {

    CART_NOT_FOUND("CART-404", "cart.not.found", "cart.not.found.title", HttpStatus.NOT_FOUND),
    ITEM_NOT_FOUND("CART-404-1", "cart.item.not.found", "cart.item.not.found.title", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND("CART-404-2", "cart.product.not.found", "cart.product.not.found.title", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_ACTIVE("CART-400", "cart.product.not.active", "cart.product.not.active.title", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_STOCK("CART-400-1", "cart.insufficient.stock", "cart.insufficient.stock.title", HttpStatus.BAD_REQUEST),
    PRODUCT_SERVICE_ERROR("CART-503", "cart.product.service.error", "cart.product.service.error.title", HttpStatus.SERVICE_UNAVAILABLE);

    private final String code;
    private final String messageKey;
    private final String titleKey;
    private final HttpStatus httpStatus;
}
