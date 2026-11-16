package com.n11bootcamp.product_service.specification;

import com.n11bootcamp.product_service.entity.Category;
import org.springframework.data.jpa.domain.Specification;
 
public class CategorySpecification {
 
    private CategorySpecification() {}
 
    public static Specification<Category> isNotDeleted() {
        return (root, query, cb) ->
                cb.isFalse(root.get("deleted"));
    }
 
    public static Specification<Category> isActive() {
        return (root, query, cb) ->
                cb.isTrue(root.get("active"));
    }
 
    public static Specification<Category> nameLike(String name) {
        return (root, query, cb) -> name == null || name.isBlank() ? null :
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }
}
 