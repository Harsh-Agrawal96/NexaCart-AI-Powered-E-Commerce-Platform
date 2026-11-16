package com.n11bootcamp.cart_service.validator;

import com.n11bootcamp.cart_service.client.dto.ProductResponse;
import com.n11bootcamp.cart_service.entity.Cart;
import com.n11bootcamp.cart_service.entity.CartItem;
import com.n11bootcamp.cart_service.exception.CartErrorCode;
import com.n11bootcamp.common.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class CartValidator {

    public void validateProductActive(ProductResponse product) {
        if (!product.active()) {
            throw new BusinessException(CartErrorCode.PRODUCT_NOT_ACTIVE);
        }
    }

    public void validateStock(ProductResponse product, Integer requestedAmount) {
        if (product.availableQuantity() < requestedAmount) {
            throw new BusinessException(CartErrorCode.INSUFFICIENT_STOCK);
        }
    }

    public Cart requireActiveCart(Optional<Cart> cartOpt, UUID userId) {
        return cartOpt.orElseThrow(() ->
                new BusinessException(CartErrorCode.CART_NOT_FOUND));
    }

    public CartItem requireCartItem(Optional<CartItem> itemOpt, UUID productId) {
        return itemOpt.orElseThrow(() ->
                new BusinessException(CartErrorCode.ITEM_NOT_FOUND));
    }

    public ProductResponse requireProductInBatch(
            Map<UUID, ProductResponse> productMap, UUID productId) {
        ProductResponse product = productMap.get(productId);
        if (product == null) {
            throw new BusinessException(CartErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }
}