package com.n11bootcamp.log_service.entity;

import com.n11bootcamp.common.base.BaseEntity;
import com.n11bootcamp.log_service.entity.enums.EventType;
import com.n11bootcamp.log_service.entity.enums.LogLevel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "saga_log_entries", indexes = {
        @Index(name = "idx_saga_order_id", columnList = "order_id"),
        @Index(name = "idx_saga_user_id", columnList = "user_id"),
        @Index(name = "idx_saga_service_name", columnList = "service_name")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaLogEntry extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private LogLevel level;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;
}
