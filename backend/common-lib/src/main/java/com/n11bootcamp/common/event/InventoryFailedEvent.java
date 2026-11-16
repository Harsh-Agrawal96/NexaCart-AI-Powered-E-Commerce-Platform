package com.n11bootcamp.common.event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryFailedEvent {
    private UUID orderId;
    private String reason;
}
