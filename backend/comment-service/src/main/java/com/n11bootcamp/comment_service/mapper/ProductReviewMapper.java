package com.n11bootcamp.comment_service.mapper;

import com.n11bootcamp.comment_service.dto.request.CreateReviewRequest;
import com.n11bootcamp.comment_service.dto.request.UpdateReviewRequest;
import com.n11bootcamp.comment_service.dto.response.ReviewResponse;
import com.n11bootcamp.comment_service.entity.ProductReview;
import org.springframework.stereotype.Component;

@Component
public class ProductReviewMapper {

    public ProductReview toEntity(CreateReviewRequest request, java.util.UUID userId) {
        return ProductReview.builder()
                .productId(request.productId())
                .userId(userId)
                .rating(request.rating())
                .comment(request.comment())
                .build();
    }

    public void updateEntity(UpdateReviewRequest request, ProductReview review) {
        if (request.rating() != null) {
            review.setRating(request.rating());
        }
        if (request.comment() != null) {
            review.setComment(request.comment());
        }
    }

    public ReviewResponse toResponse(ProductReview review) {
        return new ReviewResponse(
                review.getId(),
                review.getProductId(),
                review.getUserId(),
                null,
                null,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    public ReviewResponse toResponse(ProductReview review, String firstName, String lastName) {
        return new ReviewResponse(
                review.getId(),
                review.getProductId(),
                review.getUserId(),
                firstName,
                lastName,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}

