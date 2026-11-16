package com.n11bootcamp.product_service.validator;

import com.n11bootcamp.common.exception.BusinessException;
import com.n11bootcamp.product_service.dto.request.CreateProductRequest;
import com.n11bootcamp.product_service.entity.Product;
import com.n11bootcamp.product_service.exception.ProductErrorCode;
import com.n11bootcamp.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
 
@Component
@RequiredArgsConstructor
public class ProductValidator {
 
    private final ProductRepository productRepository;
 
    public void validateCreate(CreateProductRequest request) {
        if (productRepository.existsBySkuAndDeletedFalse(request.sku())) {
            throw new BusinessException(ProductErrorCode.PRODUCT_SKU_ALREADY_EXISTS);
        }
    }
 
    public void validateActive(Product product) {
        if (!product.isActive()) {
            throw new BusinessException(ProductErrorCode.PRODUCT_NOT_ACTIVE);
        }
    }
 
    public void validateStock(Product product, int requestedAmount) {
        if (requestedAmount <= 0) {
            throw new BusinessException(ProductErrorCode.INVALID_STOCK_AMOUNT);
        }
        if (!product.hasStock(requestedAmount)) {
            throw new BusinessException(ProductErrorCode.INSUFFICIENT_STOCK);
        }
    }
}