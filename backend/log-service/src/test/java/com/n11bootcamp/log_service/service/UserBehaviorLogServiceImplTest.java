package com.n11bootcamp.log_service.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bootcamp.common.enums.UserBehaviorType;
import com.n11bootcamp.common.event.UserBehaviorEvent;
import com.n11bootcamp.log_service.dto.response.UserBehaviorRecentItemResponse;
import com.n11bootcamp.log_service.entity.UserBehaviorLogEntry;
import com.n11bootcamp.log_service.mapper.UserBehaviorLogMapper;
import com.n11bootcamp.log_service.repository.UserBehaviorLogEntryRepository;
import com.n11bootcamp.log_service.service.impl.UserBehaviorLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserBehaviorLogServiceImplTest {

    @Mock UserBehaviorLogEntryRepository repository;

    UserBehaviorLogServiceImpl userBehaviorLogService;

    @BeforeEach
    void setUp() {
        userBehaviorLogService = new UserBehaviorLogServiceImpl(
                repository,
                new UserBehaviorLogMapper(new ObjectMapper()));
    }

    @Test
    @DisplayName("save: PRODUCT_VIEWED JSON payload ile persist")
    void save_shouldPersistUserBehaviorLogEntry() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UserBehaviorEvent event = UserBehaviorEvent.builder()
                .userId(userId)
                .productId(productId)
                .type(UserBehaviorType.PRODUCT_VIEWED)
                .occurredAt(Instant.parse("2026-05-02T10:00:00Z"))
                .build();
        given(repository.save(ArgumentMatchers.any(UserBehaviorLogEntry.class)))
                .willAnswer(inv -> inv.getArgument(0));

        UserBehaviorLogEntry saved = userBehaviorLogService.save(event);

        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getProductId()).isEqualTo(productId);
        assertThat(saved.getBehaviorType()).isEqualTo(UserBehaviorType.PRODUCT_VIEWED);
        assertThat(saved.getPayload()).contains("PRODUCT_VIEWED").contains(userId.toString());
    }

    @Test
    @DisplayName("findRecentByUserId: son kayıtlar DTO listesi (AI beslemesi)")
    void findRecentByUserId_shouldReturnMappedItems() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UserBehaviorLogEntry row = UserBehaviorLogEntry.builder()
                .userId(userId)
                .productId(productId)
                .behaviorType(UserBehaviorType.PRODUCT_VIEWED)
                .occurredAt(Instant.parse("2026-05-02T11:00:00Z"))
                .payload("{}")
                .build();
        given(repository.findByUserIdOrderByOccurredAtDesc(ArgumentMatchers.eq(userId), ArgumentMatchers.any(Pageable.class)))
                .willReturn(List.of(row));

        List<UserBehaviorRecentItemResponse> recent = userBehaviorLogService.findRecentByUserId(userId, 10);

        assertThat(recent).hasSize(1);
        assertThat(recent.getFirst().productId()).isEqualTo(productId);
        assertThat(recent.getFirst().behaviorType()).isEqualTo(UserBehaviorType.PRODUCT_VIEWED);
        assertThat(recent.getFirst().payloadJson()).isEqualTo("{}");
    }
}
