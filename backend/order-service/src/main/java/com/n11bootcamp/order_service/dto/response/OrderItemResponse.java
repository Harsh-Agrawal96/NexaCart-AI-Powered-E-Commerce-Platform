package com.n11bootcamp.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private UUID productId;
    private String productName;
    private String productSku;
    private Integer amount;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}

