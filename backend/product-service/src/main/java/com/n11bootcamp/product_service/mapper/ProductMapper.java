package com.n11bootcamp.product_service.mapper;

import com.n11bootcamp.product_service.dto.request.CreateProductRequest;
import com.n11bootcamp.product_service.dto.request.UpdateProductRequest;
import com.n11bootcamp.product_service.dto.response.ProductResponse;
import com.n11bootcamp.product_service.dto.response.ProductSummaryResponse;
import com.n11bootcamp.product_service.entity.Product;
import org.mapstruct.*;
 
@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface ProductMapper {

    @Mapping(target = "availableQuantity", expression = "java(product.getAvailableQuantity())")
    ProductResponse toResponse(Product product);

    @Mapping(target = "categoryId",   source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ProductSummaryResponse toSummaryResponse(Product product);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    @Mapping(target = "deleted",          ignore = true)
    @Mapping(target = "category",         ignore = true)
    @Mapping(target = "active",           constant = "true")
    @Mapping(target = "reservedQuantity", ignore = true)
    Product toEntity(CreateProductRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    @Mapping(target = "deleted",          ignore = true)
    @Mapping(target = "category",         ignore = true)
    @Mapping(target = "sku",              ignore = true)
    @Mapping(target = "reservedQuantity", ignore = true)
    void updateEntity(UpdateProductRequest request, @MappingTarget Product product);
}