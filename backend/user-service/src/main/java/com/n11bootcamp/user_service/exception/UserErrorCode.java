package com.n11bootcamp.user_service.exception;

import org.springframework.http.HttpStatus;

import com.n11bootcamp.common.exception.BaseErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    USER_NOT_FOUND("USER-001", "user.not.found", "user.not.found.title", HttpStatus.NOT_FOUND),
    USER_ALREADY_DELETED("USER-002", "user.already.deleted", "user.already.deleted.title", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS("USER-003", "user.email.exists", "user.email.exists.title", HttpStatus.CONFLICT),
    KEYCLOAK_USER_CREATION_FAILED("USER-004", "user.keycloak.creation.failed", "user.keycloak.creation.failed.title", HttpStatus.INTERNAL_SERVER_ERROR);


    private final String code;
    private final String messageKey;
    private final String titleKey;
    private final HttpStatus httpStatus;
}