package com.n11bootcamp.api_gateway.config;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.n11bootcamp.api_gateway.prop.RateLimitingProperties;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

@Configuration
@EnableConfigurationProperties(RateLimitingProperties.class)
public class RateLimitingConfig {
    @Bean
    RateLimiterRegistry rateLimiterRegistry(RateLimitingProperties properties) {
        RateLimitingProperties.Policy p = properties.getPolicy();

        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(p.getLimitForPeriod())
                .limitRefreshPeriod(Duration.ofMillis(p.getLimitRefreshPeriod().toMillis()))
                .timeoutDuration(Duration.ofMillis(p.getTimeoutDuration().toMillis()))
                .build();

        return RateLimiterRegistry.of(config);
    }
}

