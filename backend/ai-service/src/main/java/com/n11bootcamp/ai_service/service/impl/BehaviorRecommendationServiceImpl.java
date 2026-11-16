package com.n11bootcamp.ai_service.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.n11bootcamp.ai_service.cache.BehaviorRecommendationCache;
import com.n11bootcamp.ai_service.client.LogServiceClient;
import com.n11bootcamp.ai_service.client.ProductClient;
import com.n11bootcamp.ai_service.dto.ProductResponse;
import com.n11bootcamp.ai_service.dto.UserBehaviorRecentItemDto;
import com.n11bootcamp.ai_service.dto.request.BehaviorRecommendationRequest;
import com.n11bootcamp.ai_service.dto.response.BehaviorRecommendationResponse;
import com.n11bootcamp.ai_service.service.BehaviorRecommendationService;
import com.n11bootcamp.ai_service.service.GeminiClient;
import com.n11bootcamp.ai_service.service.parser.BehaviorRecommendationParser;
import com.n11bootcamp.ai_service.service.parser.BehaviorRecommendationParser.BehaviorRecommendationLlmResult;
import com.n11bootcamp.ai_service.service.prompt.BehaviorRecommendationPromptBuilder;
import com.n11bootcamp.ai_service.util.ApiResponses;
import com.n11bootcamp.ai_service.util.BehaviorProductIds;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BehaviorRecommendationServiceImpl implements BehaviorRecommendationService {

    private static final int MAX_CANDIDATE_PRODUCTS = 20;

    private final LogServiceClient logServiceClient;
    private final ProductClient productClient;
    private final BehaviorRecommendationPromptBuilder promptBuilder;
    private final GeminiClient geminiClient;
    private final BehaviorRecommendationParser parser;
    private final BehaviorRecommendationCache behaviorRecommendationCache;

    @Override
    public BehaviorRecommendationResponse recommend(BehaviorRecommendationRequest request) {
        Objects.requireNonNull(request.getUserId(), "userId");

        String cacheKey = cacheKey(request);
        BehaviorRecommendationResponse cached = behaviorRecommendationCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<UserBehaviorRecentItemDto> behaviors =
                logServiceClient.getRecentBehaviors(request.getUserId(), request.getLimit());
        if (behaviors == null) {
            behaviors = List.of();
        }

        BehaviorRecommendationResponse empty = new BehaviorRecommendationResponse();
        empty.setSummary("Henüz bu kullanıcı için kayıtlı davranış yok; öneri üretilemedi.");
        empty.setConfidence(0.0);
        empty.setRecommendedProducts(List.of());

        if (behaviors.isEmpty()) {
            return empty;
        }

        List<UUID> candidateIds = BehaviorProductIds.rankedUniqueByIntent(behaviors, MAX_CANDIDATE_PRODUCTS);
        List<ProductResponse> batch = ApiResponses.dataOrNull(productClient.getProductsByIds(candidateIds));
        if (batch == null) {
            batch = List.of();
        }

        Map<UUID, ProductResponse> byId = new HashMap<>();
        for (ProductResponse p : batch) {
            if (p != null && p.getId() != null) {
                byId.put(p.getId(), p);
            }
        }

        List<ProductResponse> candidatesForPrompt = orderCandidatesForPrompt(
                candidateIds, byId, request.getCurrentProductId());

        if (candidatesForPrompt.isEmpty()) {
            BehaviorRecommendationResponse out = new BehaviorRecommendationResponse();
            out.setSummary("Davranış kayıtları var ancak ürün bilgisi alınamadı.");
            out.setConfidence(0.15);
            out.setRecommendedProducts(List.of());
            return out;
        }

        String prompt = promptBuilder.build(behaviors, candidatesForPrompt, request.getCurrentProductId());
        String raw = geminiClient.generateText(prompt);
        BehaviorRecommendationLlmResult llm = parser.parse(raw);

        List<ProductResponse> ordered = orderAndFilter(llm.recommendedProductIds(), byId);

        BehaviorRecommendationResponse res = new BehaviorRecommendationResponse();
        res.setSummary(llm.summary());
        res.setConfidence(llm.confidence());
        res.setRecommendedProducts(ordered);
        behaviorRecommendationCache.put(cacheKey, res);
        return copyResponse(res);
    }

    /** userId | currentProductId veya boş | limit — ürün tavsiyesi cache’inden bağımsız. */
    static String cacheKey(BehaviorRecommendationRequest request) {
        String cp =
                request.getCurrentProductId() != null ? request.getCurrentProductId().toString() : "";
        return request.getUserId() + "|" + cp + "|" + request.getLimit();
    }

    private static BehaviorRecommendationResponse copyResponse(BehaviorRecommendationResponse src) {
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

    /**
     * Aday sırası: sepet / checkout önce (rankedUniqueByIntent). Ürün detayında mevcut ürün çıkarılır.
     */
    private static List<ProductResponse> orderCandidatesForPrompt(
            List<UUID> rankedIds, Map<UUID, ProductResponse> byId, UUID currentProductId) {
        List<ProductResponse> out = new ArrayList<>();
        for (UUID id : rankedIds) {
            ProductResponse p = byId.get(id);
            if (p == null || p.getId() == null) {
                continue;
            }
            if (currentProductId != null && currentProductId.equals(p.getId())) {
                continue;
            }
            out.add(p);
        }
        return out;
    }

    private static List<ProductResponse> orderAndFilter(List<UUID> orderedIds, Map<UUID, ProductResponse> byId) {
        if (orderedIds == null || orderedIds.isEmpty() || byId.isEmpty()) {
            return List.of();
        }
        List<ProductResponse> out = new ArrayList<>();
        for (UUID id : orderedIds) {
            ProductResponse p = byId.get(id);
            if (p != null) {
                out.add(p);
            }
        }
        return out;
    }
}
