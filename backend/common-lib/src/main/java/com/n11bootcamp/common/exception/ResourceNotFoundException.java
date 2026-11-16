package com.n11bootcamp.common.exception;

public class ResourceNotFoundException extends CoreException{
    public ResourceNotFoundException(BaseErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

}
