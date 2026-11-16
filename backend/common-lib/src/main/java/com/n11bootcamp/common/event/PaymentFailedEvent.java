package com.n11bootcamp.common.event;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentFailedEvent {
    private UUID orderId;
    private List<OrderItemPayload> items;
    private String reason;
}
