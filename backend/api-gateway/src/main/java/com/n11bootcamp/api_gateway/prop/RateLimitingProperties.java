package com.n11bootcamp.api_gateway.prop;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limiting")
public class RateLimitingProperties {

    private boolean enabled = true;

    private List<String> exemptPathPatterns = new ArrayList<>();

    private Policy policy = new Policy();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getExemptPathPatterns() {
        return exemptPathPatterns;
    }

    public void setExemptPathPatterns(List<String> exemptPathPatterns) {
        this.exemptPathPatterns = exemptPathPatterns;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public static class Policy {

        private int limitForPeriod = 20;


        private java.time.Duration limitRefreshPeriod = java.time.Duration.ofSeconds(1);


        private java.time.Duration timeoutDuration = java.time.Duration.ZERO;

        public int getLimitForPeriod() {
            return limitForPeriod;
        }

        public void setLimitForPeriod(int limitForPeriod) {
            this.limitForPeriod = limitForPeriod;
        }

        public java.time.Duration getLimitRefreshPeriod() {
            return limitRefreshPeriod;
        }

        public void setLimitRefreshPeriod(java.time.Duration limitRefreshPeriod) {
            this.limitRefreshPeriod = limitRefreshPeriod;
        }

        public java.time.Duration getTimeoutDuration() {
            return timeoutDuration;
        }

        public void setTimeoutDuration(java.time.Duration timeoutDuration) {
            this.timeoutDuration = timeoutDuration;
        }
    }
}
