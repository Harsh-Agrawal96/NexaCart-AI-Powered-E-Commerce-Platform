package com.n11bootcamp.comment_service.repository;

import com.n11bootcamp.comment_service.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID>, JpaSpecificationExecutor<ProductReview> {

    boolean existsByProductIdAndUserIdAndDeletedFalse(UUID productId, UUID userId);

    Optional<ProductReview> findByIdAndDeletedFalse(UUID id);

    Optional<ProductReview> findByProductIdAndUserIdAndDeletedFalse(UUID productId, UUID userId);
}

