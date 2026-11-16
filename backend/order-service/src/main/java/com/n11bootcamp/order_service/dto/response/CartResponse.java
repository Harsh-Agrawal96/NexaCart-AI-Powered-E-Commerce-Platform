package com.n11bootcamp.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private UUID cartId;
    private UUID userId;
    private List<CartItemResponse> items;
    private BigDecimal grandTotal;
}
