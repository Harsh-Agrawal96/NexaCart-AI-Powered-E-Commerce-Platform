package com.n11bootcamp.order_service.dto.response;

import com.n11bootcamp.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryResponse {

    private UUID id;
    private UUID userId;
    private OrderStatus status;
    private BigDecimal grandTotal;
    private LocalDateTime createdAt;
}
