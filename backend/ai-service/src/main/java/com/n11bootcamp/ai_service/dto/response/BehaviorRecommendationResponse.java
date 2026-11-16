package com.n11bootcamp.ai_service.dto.response;

import java.util.List;

import com.n11bootcamp.ai_service.dto.ProductResponse;

import lombok.Data;

@Data
public class BehaviorRecommendationResponse {

    private String summary;
    private double confidence;
    private List<ProductResponse> recommendedProducts;
}
