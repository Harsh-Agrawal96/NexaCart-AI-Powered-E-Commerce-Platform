package com.n11bootcamp.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private UUID productId;
    private String productName;
    private String productSku;
    private BigDecimal unitPrice;
    private Integer amount;
    private BigDecimal totalPrice;
}
