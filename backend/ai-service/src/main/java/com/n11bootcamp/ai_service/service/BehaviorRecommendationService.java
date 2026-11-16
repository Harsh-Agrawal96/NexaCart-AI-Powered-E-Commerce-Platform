package com.n11bootcamp.ai_service.service;

import com.n11bootcamp.ai_service.dto.request.BehaviorRecommendationRequest;
import com.n11bootcamp.ai_service.dto.response.BehaviorRecommendationResponse;

public interface BehaviorRecommendationService {

    BehaviorRecommendationResponse recommend(BehaviorRecommendationRequest request);
}
