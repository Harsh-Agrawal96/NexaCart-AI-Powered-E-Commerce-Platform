package com.n11bootcamp.ai_service.service.impl;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.n11bootcamp.ai_service.config.GeminiProperties;
import com.n11bootcamp.ai_service.service.GeminiClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiRestClient implements GeminiClient {

    private final RestTemplate restTemplate;
    private final GeminiProperties geminiProperties;

    @Override
    public String generateText(String prompt) {
        String apiKey = geminiProperties.apiKey();
        if (apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY is blank; skipping Gemini call");
            return "";
        }

        String model = geminiProperties.model();
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent";

        Map<String, Object> body = Map.of(
                "contents", new Object[]{
                        Map.of(
                                "parts", new Object[]{
                                        Map.of("text", prompt)
                                }
                        )
                }
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        Map<String, Object> res;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            res = response;
        } catch (RestClientException ex) {
            log.warn("Gemini HTTP call failed: {}", ex.toString());
            return "";
        } catch (Exception ex) {
            log.warn("Unexpected error calling Gemini: {}", ex.toString());
            return "";
        }
        if (res == null) return "";

        Object candidates = res.get("candidates");
        if (!(candidates instanceof java.util.List<?> list) || list.isEmpty()) return "";
        Object c0 = list.get(0);
        if (!(c0 instanceof Map<?, ?> c0m)) return "";
        Object content = c0m.get("content");
        if (!(content instanceof Map<?, ?> cm)) return "";
        Object parts = cm.get("parts");
        if (!(parts instanceof java.util.List<?> pl) || pl.isEmpty()) return "";
        Object p0 = pl.get(0);
        if (!(p0 instanceof Map<?, ?> p0m)) return "";
        Object text = p0m.get("text");
        return text == null ? "" : String.valueOf(text);
    }
}
