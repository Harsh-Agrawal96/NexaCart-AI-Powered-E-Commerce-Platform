package com.n11bootcamp.product_service.service.impl;

import com.n11bootcamp.common.exception.BusinessException;
import com.n11bootcamp.product_service.dto.request.CreateCategoryRequest;
import com.n11bootcamp.product_service.dto.request.UpdateCategoryRequest;
import com.n11bootcamp.product_service.dto.response.CategoryResponse;
import com.n11bootcamp.product_service.entity.Category;
import com.n11bootcamp.product_service.exception.CategoryErrorCode;
import com.n11bootcamp.product_service.mapper.CategoryMapper;
import com.n11bootcamp.product_service.repository.CategoryRepository;
import com.n11bootcamp.product_service.service.CategoryService;
import com.n11bootcamp.product_service.validator.CategoryValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
import java.util.UUID;
 
import static com.n11bootcamp.product_service.specification.CategorySpecification.*;
 
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
 
 
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CategoryValidator categoryValidator;
 
    @Override
    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        log.debug("Creating category with name: {}", request.name());
 
        categoryValidator.validateCreate(request);
 
        Category category = categoryMapper.toEntity(request);
        Category saved = categoryRepository.saveAndFlush(category);
 
        log.info("Category created with id: {}", saved.getId());
        return categoryMapper.toResponse(saved);
    }
 
    @Override
    @Transactional
    public CategoryResponse update(UUID id, UpdateCategoryRequest request) {
        log.debug("Updating category: {}", id);
 
        Category category = findByIdOrThrow(id);
        categoryValidator.validateUpdate(request, category);
 
        categoryMapper.updateEntity(request, category);
 
        log.info("Category updated: {}", id);
        return categoryMapper.toResponse(category);
    }
 
    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID id) {
        return categoryMapper.toResponse(findByIdOrThrow(id));
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActive() {
        Specification<Category> spec = Specification
                .where(isNotDeleted())
                .and(isActive());
 
        return categoryRepository.findAll(spec).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllForAdmin() {
        Specification<Category> spec = Specification
                .where(isNotDeleted());
 
        return categoryRepository.findAll(spec).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }
 
    @Override
    @Transactional
    public void delete(UUID id) {
        log.debug("Soft deleting category: {}", id);
 
        Category category = findByIdOrThrow(id);
        category.delete();
 
        log.info("Category soft deleted: {}", id);
    }
 
    // ─── Private ──────────────────────────────────────────────────────────────
 
    private Category findByIdOrThrow(UUID id) {
        return categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND));
    }
}