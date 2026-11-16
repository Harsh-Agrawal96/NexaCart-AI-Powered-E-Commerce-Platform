package com.n11bootcamp.ai_service.service.parser;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bootcamp.ai_service.dto.response.ProductAdviceResponse;
import com.n11bootcamp.ai_service.util.LlmJsonExtractUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductAdviceParser {

    private final ObjectMapper objectMapper;

    public ProductAdviceResponse parse(String rawText) {
        String raw = rawText == null ? "" : rawText;
        String json = LlmJsonExtractUtils.extractFirstJsonObject(raw);

        ProductAdviceResponse out = new ProductAdviceResponse();

        if (json == null || json.isBlank()) {
            out.setVerdict("MIXED");
            out.setSummary(raw.isBlank() ? "Yanıt alınamadı." : raw);
            out.setPros(List.of());
            out.setCons(List.of());
            out.setConfidence(0.5);
            return out;
        }

        try {
            JsonNode node = objectMapper.readTree(json);
            out.setVerdict(text(node, "verdict", "MIXED"));
            out.setSummary(text(node, "summary", raw.isBlank() ? "Yanıt alınamadı." : raw));
            out.setConfidence(number(node, "confidence", 0.5));
            out.setPros(stringList(node.get("pros")));
            out.setCons(stringList(node.get("cons")));
            return out;
        } catch (Exception e) {
            out.setVerdict("MIXED");
            out.setSummary(raw);
            out.setPros(List.of());
            out.setCons(List.of());
            out.setConfidence(0.3);
            return out;
        }
    }

    private String text(JsonNode node, String field, String def) {
        if (node == null) return def;
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return def;
        String t = v.asText();
        return t == null || t.isBlank() ? def : t;
    }

    private double number(JsonNode node, String field, double def) {
        if (node == null) return def;
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return def;
        if (v.isNumber()) return v.asDouble();
        try {
            return Double.parseDouble(v.asText());
        } catch (Exception e) {
            return def;
        }
    }

    private List<String> stringList(JsonNode arr) {
        if (arr == null || arr.isNull() || !arr.isArray()) return List.of();
        List<String> out = new ArrayList<>();
        for (JsonNode x : arr) {
            if (x != null && !x.isNull()) {
                String t = x.asText();
                if (t != null && !t.isBlank()) out.add(t);
            }
        }
        return out;
    }
}
