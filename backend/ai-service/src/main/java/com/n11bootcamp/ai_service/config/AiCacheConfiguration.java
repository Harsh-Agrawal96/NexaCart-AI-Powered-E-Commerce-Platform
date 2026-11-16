package com.n11bootcamp.ai_service.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class AiCacheConfiguration {

    public static final String PRODUCT_ADVICE_CACHE = "aiProductAdvice";
    public static final String BEHAVIOR_RECOMMENDATIONS_CACHE = "aiBehaviorRecommendations";

    @Bean
    @Primary
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                new CaffeineCache(
                        PRODUCT_ADVICE_CACHE,
                        Caffeine.newBuilder()
                                .maximumSize(5000)
                                .expireAfterWrite(30, TimeUnit.MINUTES)
                                .build()),
                new CaffeineCache(
                        BEHAVIOR_RECOMMENDATIONS_CACHE,
                        Caffeine.newBuilder()
                                .maximumSize(2000)
                                .expireAfterWrite(20, TimeUnit.SECONDS)
                                .build())));
        manager.afterPropertiesSet();
        return manager;
    }
}
