package com.n11bootcamp.cart_service.client;

import com.n11bootcamp.cart_service.client.dto.ProductResponse;
import com.n11bootcamp.common.response.ApiResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "product-service", url = "${services.product.url}")
public interface ProductServiceClient {

    @PostMapping("/api/v1/internal/products/batch")
    ApiResponse<List<ProductResponse>> getProductsByIds(@RequestBody List<UUID> ids);
}