package com.n11bootcamp.product_service.publisher;

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

    public void publishProductViewed(UUID userId, UUID productId) {
        if (userId == null) {
            return;
        }
        UserBehaviorEvent event = UserBehaviorEvent.builder()
                .userId(userId)
                .productId(productId)
                .type(UserBehaviorType.PRODUCT_VIEWED)
                .occurredAt(Instant.now())
                .build();
        try {
            rabbitTemplate.convertAndSend(
                    SagaConstants.SAGA_EXCHANGE,
                    UserBehaviorType.PRODUCT_VIEWED.routingKey(),
                    event);
            log.debug("[USER_BEHAVIOR] PRODUCT_VIEWED published — userId={}, productId={}", userId, productId);
        } catch (Exception e) {
            log.warn("[USER_BEHAVIOR] PRODUCT_VIEWED publish failed — userId={}, productId={}: {}",
                    userId, productId, e.getMessage());
        }
    }
}
