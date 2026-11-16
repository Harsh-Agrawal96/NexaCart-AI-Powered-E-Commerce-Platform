package com.n11bootcamp.product_service.exception;

import com.n11bootcamp.common.exception.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CategoryErrorCode implements BaseErrorCode {
 
    CATEGORY_NOT_FOUND("CAT-404", "category.not.found", "category.not.found.title", HttpStatus.NOT_FOUND),
    CATEGORY_NAME_ALREADY_EXISTS("CAT-409", "category.name.exists", "category.name.exists.title", HttpStatus.CONFLICT),
    CATEGORY_SLUG_ALREADY_EXISTS("CAT-409", "category.slug.exists", "category.slug.exists.title", HttpStatus.CONFLICT),
    CATEGORY_NOT_ACTIVE("CAT-422", "category.not.active", "category.not.active.title", HttpStatus.UNPROCESSABLE_ENTITY);

 
    private final String code;
    private final String messageKey;
    private final String titleKey;
    private final HttpStatus httpStatus;
}
