package com.n11bootcamp.product_service.exception;

import com.n11bootcamp.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements BaseErrorCode {
 
    PRODUCT_NOT_FOUND("PRD-404", "product.not.found", "product.not.found.title", HttpStatus.NOT_FOUND),
    PRODUCT_SKU_ALREADY_EXISTS("PRD-409", "product.sku.exists", "product.sku.exists.title", HttpStatus.CONFLICT),
    PRODUCT_NOT_ACTIVE("PRD-422", "product.not.active", "product.not.active.title", HttpStatus.UNPROCESSABLE_ENTITY),
    INSUFFICIENT_STOCK("PRD-422", "product.insufficient.stock", "product.insufficient.stock.title", HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_STOCK_AMOUNT("PRD-400", "product.invalid.stock.amount", "product.invalid.stock.amount.title", HttpStatus.BAD_REQUEST);

 
    private final String code;
    private final String messageKey;
    private final String titleKey;
    private final HttpStatus httpStatus;
}