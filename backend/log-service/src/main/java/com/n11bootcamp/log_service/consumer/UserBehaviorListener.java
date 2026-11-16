package com.n11bootcamp.log_service.consumer;

import com.n11bootcamp.common.event.UserBehaviorEvent;
import com.n11bootcamp.log_service.config.RabbitMQConfig;
import com.n11bootcamp.log_service.service.UserBehaviorLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserBehaviorListener {

    private final UserBehaviorLogService userBehaviorLogService;

    @RabbitListener(queues = RabbitMQConfig.Q_LOG_USER_BEHAVIOR)
    public void handleUserBehavior(UserBehaviorEvent event) {
        log.info("[LISTENER] UserBehaviorEvent received: type={} userId={} productId={}",
                event.getType(), event.getUserId(), event.getProductId());
        if (event.getUserId() == null || event.getProductId() == null) {
            log.warn("[LISTENER] Skipping user behavior: missing userId or productId");
            return;
        }
        userBehaviorLogService.save(event);
    }
}
