package com.n11bootcamp.ai_service.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import com.n11bootcamp.ai_service.config.AiCacheConfiguration;
import com.n11bootcamp.ai_service.dto.response.BehaviorRecommendationResponse;

@Component
public class SpringBehaviorRecommendationCache implements BehaviorRecommendationCache {

    private final Cache cache;

    public SpringBehaviorRecommendationCache(CacheManager cacheManager) {
        Cache c = cacheManager.getCache(AiCacheConfiguration.BEHAVIOR_RECOMMENDATIONS_CACHE);
        this.cache = Objects.requireNonNull(c,
                "Cache '" + AiCacheConfiguration.BEHAVIOR_RECOMMENDATIONS_CACHE + "' is not registered");
    }

    @Override
    public BehaviorRecommendationResponse getIfPresent(String key) {
        Cache.ValueWrapper w = cache.get(key);
        if (w == null) {
            return null;
        }
        Object v = w.get();
        if (!(v instanceof BehaviorRecommendationResponse response)) {
            return null;
        }
        return copy(response);
    }

    @Override
    public void put(String key, BehaviorRecommendationResponse response) {
        cache.put(key, copy(response));
    }

    private static BehaviorRecommendationResponse copy(BehaviorRecommendationResponse src) {
        BehaviorRecommendationResponse n = new BehaviorRecommendationResponse();
        n.setSummary(src.getSummary());
        n.setConfidence(src.getConfidence());
        if (src.getRecommendedProducts() != null) {
            n.setRecommendedProducts(new ArrayList<>(src.getRecommendedProducts()));
        } else {
            n.setRecommendedProducts(List.of());
        }
        return n;
    }
}
