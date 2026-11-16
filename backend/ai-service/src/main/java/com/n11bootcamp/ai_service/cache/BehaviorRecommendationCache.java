package com.n11bootcamp.ai_service.cache;

import com.n11bootcamp.ai_service.dto.response.BehaviorRecommendationResponse;


public interface BehaviorRecommendationCache {

    BehaviorRecommendationResponse getIfPresent(String key);

    void put(String key, BehaviorRecommendationResponse response);
}
