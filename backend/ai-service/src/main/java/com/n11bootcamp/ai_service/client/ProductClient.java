package com.n11bootcamp.ai_service.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.n11bootcamp.ai_service.config.FeignConfig;
import com.n11bootcamp.ai_service.dto.ProductResponse;
import com.n11bootcamp.common.response.ApiResponse;

@FeignClient(
        name = "productClient",
        url = "${services.product.url}",
        configuration = FeignConfig.class
)
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ApiResponse<ProductResponse> getProductById(@PathVariable("id") UUID id);

    @PostMapping("/api/v1/internal/products/batch")
    ApiResponse<List<ProductResponse>> getProductsByIds(@RequestBody List<UUID> ids);
}
