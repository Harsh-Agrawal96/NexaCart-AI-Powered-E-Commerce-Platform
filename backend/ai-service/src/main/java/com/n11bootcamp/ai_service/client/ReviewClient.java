package com.n11bootcamp.ai_service.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.n11bootcamp.ai_service.config.FeignConfig;
import com.n11bootcamp.ai_service.dto.ReviewResponse;
import com.n11bootcamp.common.response.ApiResponse;
import com.n11bootcamp.common.response.PageResponse;

@FeignClient(
        name = "reviewClient",
        url = "${services.review.url}",
        configuration = FeignConfig.class
)
public interface ReviewClient {

    @GetMapping("/api/v1/reviews")
    ApiResponse<PageResponse<ReviewResponse>> listByProduct(
            @RequestParam("productId") UUID productId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "ratingMin", required = false) Integer ratingMin,
            @RequestParam(value = "ratingMax", required = false) Integer ratingMax
    );
}
