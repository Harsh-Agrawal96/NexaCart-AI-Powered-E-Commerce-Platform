package com.n11bootcamp.log_service.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.n11bootcamp.common.enums.UserBehaviorType;


public record UserBehaviorRecentItemResponse(
        UUID productId,
        UserBehaviorType behaviorType,
        Instant occurredAt,
        String payloadJson
) {}
