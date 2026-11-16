package com.n11bootcamp.log_service.mapper;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bootcamp.common.event.UserBehaviorEvent;
import com.n11bootcamp.log_service.dto.response.UserBehaviorRecentItemResponse;
import com.n11bootcamp.log_service.entity.UserBehaviorLogEntry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserBehaviorLogMapper {

    private final ObjectMapper objectMapper;

    public UserBehaviorLogEntry toEntity(UserBehaviorEvent event) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(event.getType(), "event.type");
        if (event.getUserId() == null || event.getProductId() == null) {
            throw new IllegalArgumentException("userId and productId are required");
        }
        return UserBehaviorLogEntry.builder()
                .userId(event.getUserId())
                .productId(event.getProductId())
                .behaviorType(event.getType())
                .occurredAt(event.getOccurredAt() != null ? event.getOccurredAt() : Instant.now())
                .payload(payloadJsonFrom(event))
                .build();
    }

    public UserBehaviorRecentItemResponse toRecentItem(UserBehaviorLogEntry entry) {
        return new UserBehaviorRecentItemResponse(
                entry.getProductId(),
                entry.getBehaviorType(),
                entry.getOccurredAt(),
                entry.getPayload());
    }

    public List<UserBehaviorRecentItemResponse> toRecentItems(List<UserBehaviorLogEntry> entries) {
        return entries.stream().map(this::toRecentItem).toList();
    }

    private String payloadJsonFrom(UserBehaviorEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.warn("[UserBehaviorLogMapper] UserBehaviorEvent serialize edilemedi: {}", e.getMessage());
            return event.toString();
        }
    }
}
