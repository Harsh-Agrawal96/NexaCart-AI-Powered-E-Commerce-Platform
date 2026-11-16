package com.n11bootcamp.ai_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(String apiKey, String model) {

    public GeminiProperties {
        apiKey = apiKey != null ? apiKey : "";
        model = model != null && !model.isBlank() ? model : "gemini-flash-latest";
    }
}
