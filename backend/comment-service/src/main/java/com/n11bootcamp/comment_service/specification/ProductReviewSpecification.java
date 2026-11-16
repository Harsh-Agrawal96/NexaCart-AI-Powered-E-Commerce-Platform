package com.n11bootcamp.comment_service.specification;

import com.n11bootcamp.comment_service.entity.ProductReview;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class ProductReviewSpecification {

    private ProductReviewSpecification() {}

    public static Specification<ProductReview> isNotDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<ProductReview> hasProductId(UUID productId) {
        if (productId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("productId"), productId);
    }

    public static Specification<ProductReview> hasUserId(UUID userId) {
        if (userId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<ProductReview> ratingBetween(Integer min, Integer max) {
        if (min == null && max == null) {
            return null;
        }
        Integer effectiveMin = min != null ? min : 1;
        Integer effectiveMax = max != null ? max : 5;
        return (root, query, cb) -> cb.between(root.get("rating"), effectiveMin, effectiveMax);
    }
}

