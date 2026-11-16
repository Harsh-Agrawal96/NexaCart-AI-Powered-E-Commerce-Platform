package com.n11bootcamp.comment_service.service;

import com.n11bootcamp.comment_service.dto.request.CreateReviewRequest;
import com.n11bootcamp.comment_service.dto.request.UpdateReviewRequest;
import com.n11bootcamp.comment_service.dto.request.admin.AdminCreateReviewRequest;
import com.n11bootcamp.comment_service.dto.request.admin.AdminUpdateReviewRequest;
import com.n11bootcamp.comment_service.dto.response.ReviewResponse;
import com.n11bootcamp.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductReviewService {

    ReviewResponse create(UUID userId, CreateReviewRequest request);

    ReviewResponse update(UUID id, UUID userId, UpdateReviewRequest request);

    void delete(UUID id, UUID userId);

    void adminDelete(UUID id);

    ReviewResponse adminCreate(AdminCreateReviewRequest request);

    ReviewResponse adminUpdate(UUID id, AdminUpdateReviewRequest request);

    ReviewResponse adminGetById(UUID id);

    PageResponse<ReviewResponse> adminSearch(UUID productId, UUID userId, Integer ratingMin, Integer ratingMax, Pageable pageable);

    PageResponse<ReviewResponse> listByProduct(UUID productId, Integer ratingMin, Integer ratingMax, Pageable pageable);
}

