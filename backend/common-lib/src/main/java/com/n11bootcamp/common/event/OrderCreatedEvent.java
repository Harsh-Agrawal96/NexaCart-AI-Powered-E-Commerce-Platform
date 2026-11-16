package com.n11bootcamp.common.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderCreatedEvent {
    private UUID orderId;
    private UUID userId;
    private BigDecimal grandTotal;

    private List<OrderItemPayload> items;
}