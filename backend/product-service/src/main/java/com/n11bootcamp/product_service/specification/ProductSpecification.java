package com.n11bootcamp.product_service.specification;

import com.n11bootcamp.product_service.entity.Product;
import org.springframework.data.jpa.domain.Specification;
 
import java.math.BigDecimal;
import java.util.UUID;
 
public class ProductSpecification {
 
    private ProductSpecification() {}
 
    public static Specification<Product> isNotDeleted() {
        return (root, query, cb) ->
                cb.isFalse(root.get("deleted"));
    }
 
    public static Specification<Product> isActive() {
        return (root, query, cb) ->
                cb.isTrue(root.get("active"));
    }
 
    public static Specification<Product> hasCategory(UUID categoryId) {
        return (root, query, cb) -> categoryId == null ? null :
                cb.equal(root.get("category").get("id"), categoryId);
    }
 
    public static Specification<Product> nameLike(String search) {
        return (root, query, cb) -> search == null || search.isBlank() ? null :
                cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
    }
 
    public static Specification<Product> minPrice(BigDecimal min) {
        return (root, query, cb) -> min == null ? null :
                cb.greaterThanOrEqualTo(root.get("price"), min);
    }
 
    public static Specification<Product> maxPrice(BigDecimal max) {
        return (root, query, cb) -> max == null ? null :
                cb.lessThanOrEqualTo(root.get("price"), max);
    }
}
