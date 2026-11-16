package com.n11bootcamp.comment_service.service.impl;

import com.n11bootcamp.comment_service.client.UserServiceClient;
import com.n11bootcamp.comment_service.client.dto.UserResponse;
import com.n11bootcamp.comment_service.dto.request.CreateReviewRequest;
import com.n11bootcamp.comment_service.dto.request.UpdateReviewRequest;
import com.n11bootcamp.comment_service.dto.request.admin.AdminCreateReviewRequest;
import com.n11bootcamp.comment_service.dto.request.admin.AdminUpdateReviewRequest;
import com.n11bootcamp.comment_service.dto.response.ReviewResponse;
import com.n11bootcamp.comment_service.entity.ProductReview;
import com.n11bootcamp.comment_service.exception.CommentErrorCode;
import com.n11bootcamp.comment_service.mapper.ProductReviewMapper;
import com.n11bootcamp.comment_service.repository.ProductReviewRepository;
import com.n11bootcamp.comment_service.service.ProductReviewService;
import com.n11bootcamp.comment_service.specification.ProductReviewSpecification;
import com.n11bootcamp.comment_service.validator.ProductReviewValidator;
import com.n11bootcamp.common.exception.BusinessException;
import com.n11bootcamp.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository productReviewRepository;
    private final ProductReviewValidator productReviewValidator;
    private final ProductReviewMapper productReviewMapper;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional
    public ReviewResponse create(UUID userId, CreateReviewRequest request) {
        log.debug("Creating product review. productId: {}, userId: {}", request.productId(), userId);

        productReviewValidator.validateCreate(request, userId);

        ProductReview review = productReviewMapper.toEntity(request, userId);
        ProductReview saved = productReviewRepository.saveAndFlush(review);

        log.info("Product review created. id: {}", saved.getId());
        return toEnrichedResponse(saved);
    }

    @Override
    @Transactional
    public ReviewResponse update(UUID id, UUID userId, UpdateReviewRequest request) {
        log.debug("Updating product review. id: {}, userId: {}", id, userId);

        ProductReview review = findByIdOrThrow(id);
        productReviewValidator.validateOwnership(review, userId);

        if (request.rating() != null) {
            productReviewValidator.validateRating(request.rating());
        }

        productReviewMapper.updateEntity(request, review);

        log.info("Product review updated. id: {}", id);
        return toEnrichedResponse(review);
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID userId) {
        log.debug("Deleting product review (soft). id: {}, userId: {}", id, userId);

        ProductReview review = findByIdOrThrow(id);
        productReviewValidator.validateOwnership(review, userId);

        review.delete();
        log.info("Product review deleted (soft). id: {}", id);
    }

    @Override
    @Transactional
    public void adminDelete(UUID id) {
        log.debug("Admin deleting product review (soft). id: {}", id);
        ProductReview review = findByIdOrThrow(id);
        review.delete();
        log.info("Admin deleted product review (soft). id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> listByProduct(UUID productId, Integer ratingMin, Integer ratingMax, Pageable pageable) {
        productReviewValidator.validateRatingRange(ratingMin, ratingMax);

        Specification<ProductReview> spec = Specification
                .where(ProductReviewSpecification.isNotDeleted())
                .and(ProductReviewSpecification.hasProductId(productId))
                .and(ProductReviewSpecification.ratingBetween(ratingMin, ratingMax));

        return PageResponse.of(productReviewRepository.findAll(spec, pageable)
                .map(this::toEnrichedResponse));
    }

    @Override
    @Transactional
    public ReviewResponse adminCreate(AdminCreateReviewRequest request) {
        log.debug("Admin creating product review. productId: {}, userId: {}", request.productId(), request.userId());

        productReviewValidator.validateAdminCreate(request);

        ProductReview review = ProductReview.builder()
                .productId(request.productId())
                .userId(request.userId())
                .rating(request.rating())
                .comment(request.comment())
                .build();

        ProductReview saved = productReviewRepository.saveAndFlush(review);
        return toEnrichedResponse(saved);
    }

    @Override
    @Transactional
    public ReviewResponse adminUpdate(UUID id, AdminUpdateReviewRequest request) {
        ProductReview review = findByIdOrThrow(id);
        productReviewValidator.validateAdminUpdate(request);
        if (request.rating() != null) {
            review.setRating(request.rating());
        }
        if (request.comment() != null) {
            review.setComment(request.comment());
        }
        return toEnrichedResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse adminGetById(UUID id) {
        return toEnrichedResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> adminSearch(UUID productId, UUID userId, Integer ratingMin, Integer ratingMax, Pageable pageable) {
        productReviewValidator.validateRatingRange(ratingMin, ratingMax);

        Specification<ProductReview> spec = Specification
                .where(ProductReviewSpecification.isNotDeleted())
                .and(ProductReviewSpecification.hasProductId(productId))
                .and(ProductReviewSpecification.hasUserId(userId))
                .and(ProductReviewSpecification.ratingBetween(ratingMin, ratingMax));

        return PageResponse.of(productReviewRepository.findAll(spec, pageable)
                .map(this::toEnrichedResponse));
    }

    private ProductReview findByIdOrThrow(UUID id) {
        return productReviewRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(CommentErrorCode.REVIEW_NOT_FOUND));
    }

    private ReviewResponse toEnrichedResponse(ProductReview review) {
        try {
            var api = userServiceClient.getById(review.getUserId());
            UserResponse user = api != null ? api.getData() : null;
            String firstName = user != null ? user.firstName() : null;
            String lastName = user != null ? user.lastName() : null;
            return productReviewMapper.toResponse(review, firstName, lastName);
        } catch (Exception e) {
            log.warn("User enrichment failed for review userId={}: {}", review.getUserId(), e.toString());
            return productReviewMapper.toResponse(review);
        }
    }
}

