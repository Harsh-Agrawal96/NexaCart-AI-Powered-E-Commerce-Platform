package com.n11bootcamp.comment_service.validator;

import com.n11bootcamp.comment_service.dto.request.CreateReviewRequest;
import com.n11bootcamp.comment_service.dto.request.admin.AdminCreateReviewRequest;
import com.n11bootcamp.comment_service.entity.ProductReview;
import com.n11bootcamp.comment_service.exception.CommentErrorCode;
import com.n11bootcamp.comment_service.repository.ProductReviewRepository;
import com.n11bootcamp.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductReviewValidatorTest {

    @Mock
    ProductReviewRepository productReviewRepository;

    @Test
    @DisplayName("rating aralığı dışında ise INVALID_RATING fırlatır")
    void validateRating_shouldThrow_whenOutOfRange() {
        ProductReviewValidator validator = new ProductReviewValidator(productReviewRepository);

        assertThatThrownBy(() -> validator.validateRating(0))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(CommentErrorCode.INVALID_RATING));

        assertThatThrownBy(() -> validator.validateRating(6))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(CommentErrorCode.INVALID_RATING));
    }

    @Test
    @DisplayName("ratingMin > ratingMax ise INVALID_RATING fırlatır")
    void validateRatingRange_shouldThrow_whenMinGreaterThanMax() {
        ProductReviewValidator validator = new ProductReviewValidator(productReviewRepository);

        assertThatThrownBy(() -> validator.validateRatingRange(5, 1))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(CommentErrorCode.INVALID_RATING));
    }

    @Test
    @DisplayName("user aynı ürüne ikinci kez review yazarsa REVIEW_ALREADY_EXISTS fırlatır")
    void validateCreate_shouldThrow_whenAlreadyExists() {
        ProductReviewValidator validator = new ProductReviewValidator(productReviewRepository);

        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        given(productReviewRepository.existsByProductIdAndUserIdAndDeletedFalse(productId, userId))
                .willReturn(true);

        CreateReviewRequest request = new CreateReviewRequest(productId, 5, "güzel");

        assertThatThrownBy(() -> validator.validateCreate(request, userId))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(CommentErrorCode.REVIEW_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("admin create: review varsa REVIEW_ALREADY_EXISTS fırlatır")
    void validateAdminCreate_shouldThrow_whenAlreadyExists() {
        ProductReviewValidator validator = new ProductReviewValidator(productReviewRepository);

        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        given(productReviewRepository.existsByProductIdAndUserIdAndDeletedFalse(productId, userId))
                .willReturn(true);

        AdminCreateReviewRequest request = new AdminCreateReviewRequest(productId, userId, 4, "idare eder");

        assertThatThrownBy(() -> validator.validateAdminCreate(request))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(CommentErrorCode.REVIEW_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("validateOwnership: kullanıcı review sahibi değilse REVIEW_ACCESS_DENIED fırlatır")
    void validateOwnership_shouldThrow_whenNotOwner() {
        ProductReviewValidator validator = new ProductReviewValidator(productReviewRepository);

        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        ProductReview review = ProductReview.builder()
                .productId(UUID.randomUUID())
                .userId(ownerId)
                .rating(5)
                .comment("x")
                .build();

        assertThatThrownBy(() -> validator.validateOwnership(review, otherUserId))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(CommentErrorCode.REVIEW_ACCESS_DENIED));
    }
}

