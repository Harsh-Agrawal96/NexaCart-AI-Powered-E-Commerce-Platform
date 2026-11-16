package com.n11bootcamp.common.exception;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
    String getCode();

    String getMessageKey();

    String getTitleKey();

    HttpStatus getHttpStatus();
}
