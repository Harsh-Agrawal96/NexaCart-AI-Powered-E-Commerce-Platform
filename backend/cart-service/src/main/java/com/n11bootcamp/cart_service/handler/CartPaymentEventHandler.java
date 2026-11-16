package com.n11bootcamp.cart_service.handler;

import com.n11bootcamp.cart_service.service.CartService;
import com.n11bootcamp.common.event.PaymentSucceededEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class CartPaymentEventHandler {

    private final CartService cartService;

    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        log.info("PaymentSucceededEvent handling. orderId={}, userId={}",
                event.getOrderId(), event.getUserId());
        try {
            cartService.clearCart(event.getUserId());
            log.info("Cart cleared successfully. userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to clear cart. userId={}, orderId={}",
                    event.getUserId(), event.getOrderId(), e);
            throw e;
        }
    }
}
