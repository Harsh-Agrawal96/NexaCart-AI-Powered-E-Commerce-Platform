package com.n11bootcamp.ai_service.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.n11bootcamp.common.enums.UserBehaviorType;

import lombok.Data;

/**
 * log-service {@code UserBehaviorRecentItemResponse} ile uyumlu DTO.
 */
@Data
public class UserBehaviorRecentItemDto {

    private UUID productId;
    private UserBehaviorType behaviorType;
    private Instant occurredAt;

    @JsonProperty("payloadJson")
    private String payloadJson;
}
