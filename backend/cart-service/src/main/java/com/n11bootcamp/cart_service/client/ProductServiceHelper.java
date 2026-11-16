package com.n11bootcamp.cart_service.client;

import com.n11bootcamp.cart_service.client.dto.ProductResponse;
import com.n11bootcamp.cart_service.exception.CartErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceHelper {

    private final ProductServiceClient productServiceClient;

    public Map<UUID, ProductResponse> fetchProductMap(List<UUID> ids) {
        try {
            var response = productServiceClient.getProductsByIds(ids);
            if (response == null || response.getData() == null) {
                log.warn("Product service returned null for ids={}", ids);
                return Collections.emptyMap();
            }
            return response.getData().stream()
                    .collect(Collectors.toMap(ProductResponse::id, p -> p));
        } catch (Exception e) {
            log.error("Failed to fetch products from product-service. ids={}", ids, e);
            throw new com.n11bootcamp.common.exception.BusinessException(CartErrorCode.PRODUCT_SERVICE_ERROR);
        }
    }
}