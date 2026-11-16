package com.n11bootcamp.cart_service.publisher;

import java.time.Instant;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.n11bootcamp.common.constant.SagaConstants;
import com.n11bootcamp.common.enums.UserBehaviorType;
import com.n11bootcamp.common.event.UserBehaviorEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserBehaviorEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishAddToCart(UUID userId, UUID productId) {
        if (userId == null) {
            return;
        }
        UserBehaviorEvent event = UserBehaviorEvent.builder()
                .userId(userId)
                .productId(productId)
                .type(UserBehaviorType.ADD_TO_CART)
                .occurredAt(Instant.now())
                .build();
        try {
            rabbitTemplate.convertAndSend(
                    SagaConstants.SAGA_EXCHANGE,
                    UserBehaviorType.ADD_TO_CART.routingKey(),
                    event);
            log.debug("[USER_BEHAVIOR] ADD_TO_CART published — userId={}, productId={}", userId, productId);
        } catch (Exception e) {
            log.warn("[USER_BEHAVIOR] ADD_TO_CART publish failed — userId={}, productId={}: {}",
                    userId, productId, e.getMessage());
        }
    }

    public void publishRemoveFromCart(UUID userId, UUID productId) {
        if (userId == null) {
            return;
        }
        UserBehaviorEvent event = UserBehaviorEvent.builder()
                .userId(userId)
                .productId(productId)
                .type(UserBehaviorType.REMOVE_FROM_CART)
                .occurredAt(Instant.now())
                .build();
        try {
            rabbitTemplate.convertAndSend(
                    SagaConstants.SAGA_EXCHANGE,
                    UserBehaviorType.REMOVE_FROM_CART.routingKey(),
                    event);
            log.debug("[USER_BEHAVIOR] REMOVE_FROM_CART published — userId={}, productId={}", userId, productId);
        } catch (Exception e) {
            log.warn("[USER_BEHAVIOR] REMOVE_FROM_CART publish failed — userId={}, productId={}: {}",
                    userId, productId, e.getMessage());
        }
    }
}
