package com.n11bootcamp.common.event;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.n11bootcamp.common.enums.UserBehaviorType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserBehaviorEvent {

    private UUID userId;

    private UUID productId;

    private UserBehaviorType type;

    private Instant occurredAt;
}
