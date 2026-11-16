package com.n11bootcamp.order_service.client;

import com.n11bootcamp.common.exception.BusinessException;
import com.n11bootcamp.order_service.dto.response.CartResponse;
import com.n11bootcamp.order_service.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartServiceHelper {

    private final CartServiceClient cartServiceClient;

    public CartResponse getActiveCart(UUID userId) {
        log.info("Fetching active cart for userId: {}", userId);

        CartResponse cart;
        try {
            cart = cartServiceClient.getActiveCart(userId);
        } catch (Exception e) {
            log.error("Failed to fetch cart for userId: {}", userId, e);
            throw new BusinessException(OrderErrorCode.CART_NOT_FOUND);
        }

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            log.warn("Cart is empty for userId: {}", userId);
            throw new BusinessException(OrderErrorCode.CART_EMPTY);
        }

        return cart;
    }
}