package com.n11bootcamp.log_service.entity;

import java.time.Instant;
import java.util.UUID;

import com.n11bootcamp.common.base.BaseEntity;
import com.n11bootcamp.common.enums.UserBehaviorType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_behavior_log_entries", indexes = {
        @Index(name = "idx_ub_user_occurred", columnList = "user_id, occurred_at")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorLogEntry extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "behavior_type", nullable = false, length = 64)
    private UserBehaviorType behaviorType;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;
}
