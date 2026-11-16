package com.n11bootcamp.product_service.mapper;

import com.n11bootcamp.product_service.dto.request.CreateCategoryRequest;
import com.n11bootcamp.product_service.dto.request.UpdateCategoryRequest;
import com.n11bootcamp.product_service.dto.response.CategoryResponse;
import com.n11bootcamp.product_service.entity.Category;
import org.mapstruct.*;
 
@Mapper(componentModel = "spring")
public interface CategoryMapper {
 
    CategoryResponse toResponse(Category category);
 
    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "displayOrder", expression = "java(request.displayOrder() != null ? request.displayOrder() : 0)")
    Category toEntity(CreateCategoryRequest request);
 
    // null gelen alan → entity'de mevcut değer korunur
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "products", ignore = true)
    void updateEntity(UpdateCategoryRequest request, @MappingTarget Category category);
}
