package com.n11bootcamp.comment_service.service;

import com.n11bootcamp.comment_service.client.UserServiceClient;
import com.n11bootcamp.comment_service.client.dto.UserResponse;
import com.n11bootcamp.comment_service.dto.request.CreateReviewRequest;
import com.n11bootcamp.comment_service.dto.request.UpdateReviewRequest;
import com.n11bootcamp.comment_service.dto.response.ReviewResponse;
import com.n11bootcamp.comment_service.entity.ProductReview;
import com.n11bootcamp.comment_service.exception.CommentErrorCode;
import com.n11bootcamp.comment_service.mapper.ProductReviewMapper;
import com.n11bootcamp.comment_service.repository.ProductReviewRepository;
import com.n11bootcamp.comment_service.service.impl.ProductReviewServiceImpl;
import com.n11bootcamp.comment_service.validator.ProductReviewValidator;
import com.n11bootcamp.common.exception.BusinessException;
import com.n11bootcamp.common.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductReviewServiceImplTest {

    @Mock
    ProductReviewRepository productReviewRepository;

    @Mock
    ProductReviewValidator productReviewValidator;

    @Mock
    ProductReviewMapper productReviewMapper;

    @Mock
    UserServiceClient userServiceClient;

    @InjectMocks
    ProductReviewServiceImpl productReviewService;

    @Test
    @DisplayName("create: mapper->entity, save, response döner")
    void create_shouldSaveAndReturnResponse() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CreateReviewRequest request = new CreateReviewRequest(productId, 5, "harika");
        ProductReview entity = ProductReview.builder()
                .productId(productId)
                .userId(userId)
                .rating(5)
                .comment("harika")
                .build();

        ProductReview saved = ProductReview.builder()
                .productId(productId)
                .userId(userId)
                .rating(5)
                .comment("harika")
                .build();

        ReviewResponse response = new ReviewResponse(null, productId, userId, "Ali", "Veli", 5, "harika", null, null);

        given(productReviewMapper.toEntity(request, userId)).willReturn(entity);
        given(productReviewRepository.saveAndFlush(entity)).willReturn(saved);
        given(userServiceClient.getById(userId))
                .willReturn(ApiResponse.ok(new UserResponse(userId, "Ali", "Veli", "a@b.com")));
        given(productReviewMapper.toResponse(saved, "Ali", "Veli")).willReturn(response);

        ReviewResponse result = productReviewService.create(userId, request);

        verify(productReviewValidator).validateCreate(request, userId);
        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.rating()).isEqualTo(5);
    }

    @Test
    @DisplayName("update: review bulunamazsa REVIEW_NOT_FOUND fırlatır")
    void update_shouldThrow_whenNotFound() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(productReviewRepository.findByIdAndDeletedFalse(reviewId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productReviewService.update(reviewId, userId, new UpdateReviewRequest(5, "x")))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(CommentErrorCode.REVIEW_NOT_FOUND));
    }

    @Test
    @DisplayName("delete: ownership validator çağrılır, soft delete yapılır")
    void delete_shouldValidateOwnershipAndSoftDelete() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        ProductReview review = ProductReview.builder()
                .productId(productId)
                .userId(userId)
                .rating(4)
                .comment("ok")
                .build();

        given(productReviewRepository.findByIdAndDeletedFalse(reviewId)).willReturn(Optional.of(review));

        productReviewService.delete(reviewId, userId);

        verify(productReviewValidator).validateOwnership(review, userId);
        assertThat(review.isDeleted()).isTrue();
    }
}

