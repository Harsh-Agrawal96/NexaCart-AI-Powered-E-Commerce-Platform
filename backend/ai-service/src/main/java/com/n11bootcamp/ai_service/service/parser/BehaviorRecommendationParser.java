package com.n11bootcamp.ai_service.service.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bootcamp.ai_service.util.LlmJsonExtractUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BehaviorRecommendationParser {

    private final ObjectMapper objectMapper;

    public BehaviorRecommendationLlmResult parse(String rawText) {
        String raw = rawText == null ? "" : rawText;
        String json = LlmJsonExtractUtils.extractFirstJsonObject(raw);

        if (json == null || json.isBlank()) {
            return new BehaviorRecommendationLlmResult(
                    raw.isBlank() ? "Yanıt alınamadı." : raw,
                    0.25,
                    List.of());
        }

        try {
            JsonNode node = objectMapper.readTree(json);
            String summary = text(node, "summary", raw.isBlank() ? "Özet üretilemedi." : raw);
            double confidence = number(node, "confidence", 0.5);
            List<UUID> ids = uuidList(node.get("recommendedProductIds"));
            return new BehaviorRecommendationLlmResult(summary, confidence, ids);
        } catch (Exception e) {
            return new BehaviorRecommendationLlmResult(raw, 0.25, List.of());
        }
    }

    public record BehaviorRecommendationLlmResult(String summary, double confidence, List<UUID> recommendedProductIds) {}

    private String text(JsonNode node, String field, String def) {
        if (node == null) {
            return def;
        }
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return def;
        }
        String t = v.asText();
        return t == null || t.isBlank() ? def : t;
    }

    private double number(JsonNode node, String field, double def) {
        if (node == null) {
            return def;
        }
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return def;
        }
        if (v.isNumber()) {
            return v.asDouble();
        }
        try {
            return Double.parseDouble(v.asText());
        } catch (Exception e) {
            return def;
        }
    }

    private List<UUID> uuidList(JsonNode arr) {
        if (arr == null || arr.isNull() || !arr.isArray()) {
            return List.of();
        }
        List<UUID> out = new ArrayList<>();
        for (JsonNode x : arr) {
            if (x == null || x.isNull()) {
                continue;
            }
            try {
                String t = x.asText();
                if (t != null && !t.isBlank()) {
                    out.add(UUID.fromString(t.trim()));
                }
            } catch (Exception ignored) {
                // atla
            }
        }
        return out;
    }
}
