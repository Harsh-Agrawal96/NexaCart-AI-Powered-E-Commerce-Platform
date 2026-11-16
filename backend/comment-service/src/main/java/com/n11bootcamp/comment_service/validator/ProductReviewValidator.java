package com.n11bootcamp.comment_service.validator;

import com.n11bootcamp.comment_service.dto.request.CreateReviewRequest;
import com.n11bootcamp.comment_service.dto.request.admin.AdminCreateReviewRequest;
import com.n11bootcamp.comment_service.dto.request.admin.AdminUpdateReviewRequest;
import com.n11bootcamp.comment_service.entity.ProductReview;
import com.n11bootcamp.comment_service.exception.CommentErrorCode;
import com.n11bootcamp.comment_service.repository.ProductReviewRepository;
import com.n11bootcamp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductReviewValidator {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private final ProductReviewRepository productReviewRepository;

    public void validateCreate(CreateReviewRequest request, UUID userId) {
        validateRating(request.rating());
        if (productReviewRepository.existsByProductIdAndUserIdAndDeletedFalse(request.productId(), userId)) {
            throw new BusinessException(CommentErrorCode.REVIEW_ALREADY_EXISTS);
        }
    }

    public void validateAdminCreate(AdminCreateReviewRequest request) {
        validateRating(request.rating());
        if (productReviewRepository.existsByProductIdAndUserIdAndDeletedFalse(request.productId(), request.userId())) {
            throw new BusinessException(CommentErrorCode.REVIEW_ALREADY_EXISTS);
        }
    }

    public void validateAdminUpdate(AdminUpdateReviewRequest request) {
        if (request.rating() != null) {
            validateRating(request.rating());
        }
    }

    public void validateRatingRange(Integer min, Integer max) {
        int effectiveMin = min != null ? min : MIN_RATING;
        int effectiveMax = max != null ? max : MAX_RATING;
        if (effectiveMin > effectiveMax) {
            throw new BusinessException(CommentErrorCode.INVALID_RATING);
        }
        validateRating(effectiveMin);
        validateRating(effectiveMax);
    }

    public void validateRating(Integer rating) {
        if (rating == null) {
            throw new BusinessException(CommentErrorCode.INVALID_RATING);
        }
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new BusinessException(CommentErrorCode.INVALID_RATING);
        }
    }

    public void validateOwnership(ProductReview review, UUID userId) {
        if (!Objects.equals(review.getUserId(), userId)) {
            throw new BusinessException(CommentErrorCode.REVIEW_ACCESS_DENIED);
        }
    }
}

