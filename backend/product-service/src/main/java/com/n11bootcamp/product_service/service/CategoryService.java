package com.n11bootcamp.product_service.service;

import com.n11bootcamp.product_service.dto.request.CreateCategoryRequest;
import com.n11bootcamp.product_service.dto.request.UpdateCategoryRequest;
import com.n11bootcamp.product_service.dto.response.CategoryResponse;
 
import java.util.List;
import java.util.UUID;
 
public interface CategoryService {
 
    CategoryResponse create(CreateCategoryRequest request);
 
    CategoryResponse update(UUID id, UpdateCategoryRequest request);
 
    CategoryResponse getById(UUID id);
 
    List<CategoryResponse> getAllActive();
 
    List<CategoryResponse> getAllForAdmin();
 
    void delete(UUID id);
}
