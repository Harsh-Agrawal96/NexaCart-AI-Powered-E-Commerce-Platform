package com.n11bootcamp.ai_service.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.n11bootcamp.ai_service.client.LogServiceClient;
import com.n11bootcamp.ai_service.cache.BehaviorRecommendationCache;
import com.n11bootcamp.ai_service.client.ProductClient;
import com.n11bootcamp.ai_service.dto.ProductResponse;
import com.n11bootcamp.ai_service.dto.UserBehaviorRecentItemDto;
import com.n11bootcamp.ai_service.dto.request.BehaviorRecommendationRequest;
import com.n11bootcamp.ai_service.dto.response.BehaviorRecommendationResponse;
import com.n11bootcamp.ai_service.service.GeminiClient;
import com.n11bootcamp.ai_service.service.parser.BehaviorRecommendationParser;
import com.n11bootcamp.ai_service.service.parser.BehaviorRecommendationParser.BehaviorRecommendationLlmResult;
import com.n11bootcamp.ai_service.service.prompt.BehaviorRecommendationPromptBuilder;
import com.n11bootcamp.common.enums.UserBehaviorType;
import com.n11bootcamp.common.response.ApiResponse;

@ExtendWith(MockitoExtension.class)
class BehaviorRecommendationServiceImplTest {

    @Mock LogServiceClient logServiceClient;
    @Mock ProductClient productClient;
    @Mock BehaviorRecommendationPromptBuilder promptBuilder;
    @Mock GeminiClient geminiClient;
    @Mock BehaviorRecommendationParser parser;
    @Mock BehaviorRecommendationCache behaviorRecommendationCache;

    @InjectMocks
    BehaviorRecommendationServiceImpl service;

    @BeforeEach
    void cacheDefaultsToMiss() {
        lenient().when(behaviorRecommendationCache.getIfPresent(anyString())).thenReturn(null);
    }

    @Test
    @DisplayName("Davranış yoksa boş özet (Gemini çağrılmaz)")
    void recommend_whenNoBehaviors_returnsEmptyMessage() {
        UUID userId = UUID.randomUUID();
        given(logServiceClient.getRecentBehaviors(eq(userId), anyInt())).willReturn(List.of());

        BehaviorRecommendationRequest req = new BehaviorRecommendationRequest();
        req.setUserId(userId);
        req.setLimit(10);

        BehaviorRecommendationResponse out = service.recommend(req);

        assertThat(out.getRecommendedProducts()).isEmpty();
        assertThat(out.getConfidence()).isEqualTo(0.0);
        assertThat(out.getSummary()).contains("davranış");
    }

    @Test
    @DisplayName("Davranış + ürün varsa Gemini ve sıralama uygulanır")
    void recommend_happyPath() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        var row = new UserBehaviorRecentItemDto();
        row.setProductId(productId);
        row.setBehaviorType(UserBehaviorType.PRODUCT_VIEWED);
        row.setOccurredAt(Instant.parse("2026-05-02T10:00:00Z"));

        given(logServiceClient.getRecentBehaviors(userId, 10)).willReturn(List.of(row));

        ProductResponse pr = new ProductResponse();
        pr.setId(productId);
        pr.setName("Kulaklık");
        pr.setPrice(BigDecimal.TEN);
        pr.setCategoryName("Aksesuar");
        given(productClient.getProductsByIds(anyList())).willReturn(ApiResponse.ok(List.of(pr)));

        given(promptBuilder.build(anyList(), anyList(), eq(null))).willReturn("prompt");
        given(geminiClient.generateText("prompt")).willReturn(
                "{\"summary\":\"x\",\"confidence\":0.9,\"recommendedProductIds\":[\"" + productId + "\"]}");
        given(parser.parse(anyString()))
                .willReturn(new BehaviorRecommendationLlmResult("x", 0.9, List.of(productId)));

        BehaviorRecommendationRequest req = new BehaviorRecommendationRequest();
        req.setUserId(userId);
        req.setLimit(10);

        BehaviorRecommendationResponse out = service.recommend(req);

        assertThat(out.getSummary()).isEqualTo("x");
        assertThat(out.getConfidence()).isEqualTo(0.9);
        assertThat(out.getRecommendedProducts()).hasSize(1);
        assertThat(out.getRecommendedProducts().getFirst().getName()).isEqualTo("Kulaklık");

        verify(behaviorRecommendationCache).put(eq(BehaviorRecommendationServiceImpl.cacheKey(req)), any());
    }

    @Test
    @DisplayName("Cache varken Gemini ve ürün zinciri atlanır (20 sn içinde son cevap)")
    void recommend_cacheHit_skipsDownstream() {
        UUID userId = UUID.randomUUID();

        BehaviorRecommendationRequest req = new BehaviorRecommendationRequest();
        req.setUserId(userId);
        req.setLimit(10);
        String key = BehaviorRecommendationServiceImpl.cacheKey(req);

        BehaviorRecommendationResponse cached = new BehaviorRecommendationResponse();
        cached.setSummary("Önbellekten");
        cached.setConfidence(0.5);
        cached.setRecommendedProducts(List.of());

        given(behaviorRecommendationCache.getIfPresent(key)).willReturn(cached);

        BehaviorRecommendationResponse out = service.recommend(req);

        assertThat(out.getSummary()).isEqualTo("Önbellekten");
        verify(logServiceClient, never()).getRecentBehaviors(any(), anyInt());
        verify(productClient, never()).getProductsByIds(anyList());
        verify(geminiClient, never()).generateText(anyString());
    }
}
