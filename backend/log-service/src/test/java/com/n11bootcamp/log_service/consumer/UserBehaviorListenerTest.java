package com.n11bootcamp.log_service.consumer;

import com.n11bootcamp.common.enums.UserBehaviorType;
import com.n11bootcamp.common.event.UserBehaviorEvent;
import com.n11bootcamp.log_service.service.UserBehaviorLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserBehaviorListenerTest {

    @Mock UserBehaviorLogService userBehaviorLogService;
    @InjectMocks UserBehaviorListener userBehaviorListener;

    @Test
    @DisplayName("UserBehaviorEvent gelince: saveUserBehaviorEvent ile persist edilir")
    void handleUserBehavior_shouldDelegateToService() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UserBehaviorEvent event = UserBehaviorEvent.builder()
                .userId(userId)
                .productId(productId)
                .type(UserBehaviorType.PRODUCT_VIEWED)
                .occurredAt(Instant.parse("2026-05-02T12:00:00Z"))
                .build();

        userBehaviorListener.handleUserBehavior(event);

        ArgumentCaptor<UserBehaviorEvent> captor = ArgumentCaptor.forClass(UserBehaviorEvent.class);
        then(userBehaviorLogService).should().save(captor.capture());
        assertThat(captor.getValue()).isSameAs(event);
    }
}
