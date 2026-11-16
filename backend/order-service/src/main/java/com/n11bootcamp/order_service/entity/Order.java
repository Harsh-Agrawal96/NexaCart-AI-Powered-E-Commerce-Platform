package com.n11bootcamp.order_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.n11bootcamp.common.base.BaseEntity;
import com.n11bootcamp.common.enums.OrderStatus;

@Entity
@Table(
    name = "orders",
    indexes = {
        @Index(name = "idx_order_status",  columnList = "status"),
        @Index(name = "idx_order_user_id", columnList = "userId")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Column(nullable = false)
    private UUID userId;

    @Column(precision = 19, scale = 4)
    private BigDecimal grandTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column
    private String cancelReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}
