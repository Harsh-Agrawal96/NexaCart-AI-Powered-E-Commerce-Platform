package com.n11bootcamp.comment_service.exception;

import com.n11bootcamp.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements BaseErrorCode {

    REVIEW_NOT_FOUND("CMT-404", "review.not.found", "review.not.found.title", HttpStatus.NOT_FOUND),
    REVIEW_ACCESS_DENIED("CMT-403", "review.access.denied", "review.access.denied.title", HttpStatus.FORBIDDEN),
    REVIEW_ALREADY_EXISTS("CMT-409", "review.already.exists", "review.already.exists.title", HttpStatus.CONFLICT),
    INVALID_RATING("CMT-400", "review.invalid.rating", "review.invalid.rating.title", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String messageKey;
    private final String titleKey;
    private final HttpStatus httpStatus;
}

