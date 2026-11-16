package com.n11bootcamp.api_gateway.prop;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway-resilience")
public class GatewayResilienceProperties {

    private boolean enabled = true;

    private List<String> protectedPathPatterns = new ArrayList<>();

    private List<String> exemptPathPatterns = new ArrayList<>();

    private String circuitBreakerInstance = "paymentGateway";
    private String timeLimiterInstance = "paymentGateway";

    private Duration fallbackTimeout = Duration.ofSeconds(2);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getProtectedPathPatterns() {
        return protectedPathPatterns;
    }

    public void setProtectedPathPatterns(List<String> protectedPathPatterns) {
        this.protectedPathPatterns = protectedPathPatterns;
    }

    public List<String> getExemptPathPatterns() {
        return exemptPathPatterns;
    }

    public void setExemptPathPatterns(List<String> exemptPathPatterns) {
        this.exemptPathPatterns = exemptPathPatterns;
    }

    public String getCircuitBreakerInstance() {
        return circuitBreakerInstance;
    }

    public void setCircuitBreakerInstance(String circuitBreakerInstance) {
        this.circuitBreakerInstance = circuitBreakerInstance;
    }

    public String getTimeLimiterInstance() {
        return timeLimiterInstance;
    }

    public void setTimeLimiterInstance(String timeLimiterInstance) {
        this.timeLimiterInstance = timeLimiterInstance;
    }

    public Duration getFallbackTimeout() {
        return fallbackTimeout;
    }

    public void setFallbackTimeout(Duration fallbackTimeout) {
        this.fallbackTimeout = fallbackTimeout;
    }
}

