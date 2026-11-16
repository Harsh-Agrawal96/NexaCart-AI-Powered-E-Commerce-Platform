package com.n11bootcamp.common.exception;

public class BusinessException extends CoreException{
    public BusinessException(BaseErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

}
