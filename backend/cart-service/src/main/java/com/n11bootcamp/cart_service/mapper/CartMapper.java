package com.n11bootcamp.cart_service.mapper;

import com.n11bootcamp.cart_service.client.dto.ProductResponse;
import com.n11bootcamp.cart_service.dto.response.CartItemResponse;
import com.n11bootcamp.cart_service.dto.response.CartResponse;
import com.n11bootcamp.cart_service.dto.response.CartSummaryResponse;
import com.n11bootcamp.cart_service.entity.Cart;
import com.n11bootcamp.cart_service.entity.CartItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class CartMapper {

    public CartItemResponse toCartItemResponse(CartItem item, ProductResponse product) {
        BigDecimal totalPrice = product.price()
                .multiply(BigDecimal.valueOf(item.getAmount()));

        return new CartItemResponse(
                item.getProductId(),
                product.name(),
                product.sku(),
                product.price(),
                item.getAmount(),
                totalPrice
        );
    }

    public CartResponse toCartResponse(Cart cart, Map<UUID, ProductResponse> productMap) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> {
                    ProductResponse product = productMap.get(item.getProductId());
                    if (product == null) {
                        log.warn("Product not found in batch response. productId={}",
                                item.getProductId());
                        return null;
                    }
                    return toCartItemResponse(item, product);
                })
                .filter(item -> item != null)
                .toList();

        BigDecimal grandTotal = itemResponses.stream()
                .map(CartItemResponse::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), cart.getUserId(), itemResponses, grandTotal);
    }

    public CartResponse toEmptyCartResponse(Cart cart, UUID userId) {
        return new CartResponse(
                cart != null ? cart.getId() : null,
                userId,
                Collections.emptyList(),
                BigDecimal.ZERO
        );
    }

    public CartSummaryResponse toSummaryResponse(Cart cart) {
        return new CartSummaryResponse(
                cart.getId(),
                cart.getUserId(),
                cart.getStatus(),
                cart.getItems().size(),
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }
}