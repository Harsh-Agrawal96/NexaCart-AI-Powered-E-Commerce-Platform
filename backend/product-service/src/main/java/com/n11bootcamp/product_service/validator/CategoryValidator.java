package com.n11bootcamp.product_service.validator;

import com.n11bootcamp.common.exception.BusinessException;
import com.n11bootcamp.product_service.dto.request.CreateCategoryRequest;
import com.n11bootcamp.product_service.dto.request.UpdateCategoryRequest;
import com.n11bootcamp.product_service.entity.Category;
import com.n11bootcamp.product_service.exception.CategoryErrorCode;
import com.n11bootcamp.product_service.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryValidator {
 
    private final CategoryRepository categoryRepository;
 
    public void validateCreate(CreateCategoryRequest request) {
        if (categoryRepository.existsByNameAndDeletedFalse(request.name())) {
            throw new BusinessException(CategoryErrorCode.CATEGORY_NAME_ALREADY_EXISTS);
        }
        if (categoryRepository.existsBySlugAndDeletedFalse(request.slug())) {
            throw new BusinessException(CategoryErrorCode.CATEGORY_SLUG_ALREADY_EXISTS);
        }
    }
 
    public void validateUpdate(UpdateCategoryRequest request, Category existing) {
        if (request.name() != null && !request.name().equals(existing.getName())) {
            if (categoryRepository.existsByNameAndDeletedFalse(request.name())) {
                throw new BusinessException(CategoryErrorCode.CATEGORY_NAME_ALREADY_EXISTS);
            }
        }
        if (request.slug() != null && !request.slug().equals(existing.getSlug())) {
            if (categoryRepository.existsBySlugAndDeletedFalse(request.slug())) {
                throw new BusinessException(CategoryErrorCode.CATEGORY_SLUG_ALREADY_EXISTS);
            }
        }
    }
}