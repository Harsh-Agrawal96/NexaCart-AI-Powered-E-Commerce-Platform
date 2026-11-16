package com.n11bootcamp.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.n11bootcamp.product_service.entity.Category;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {
 
    Optional<Category> findByIdAndDeletedFalse(UUID id);
 
    boolean existsByNameAndDeletedFalse(String name);
 
    boolean existsBySlugAndDeletedFalse(String slug);
}