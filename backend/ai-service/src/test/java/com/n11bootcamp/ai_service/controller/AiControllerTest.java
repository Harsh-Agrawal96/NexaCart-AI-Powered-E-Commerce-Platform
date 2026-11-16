package com.n11bootcamp.ai_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bootcamp.ai_service.dto.request.BehaviorRecommendationRequest;
import com.n11bootcamp.ai_service.dto.request.ProductAdviceRequest;
import com.n11bootcamp.ai_service.dto.response.BehaviorRecommendationResponse;
import com.n11bootcamp.ai_service.dto.response.ProductAdviceResponse;
import com.n11bootcamp.ai_service.service.BehaviorRecommendationService;
import com.n11bootcamp.ai_service.service.ProductAdviceService;


@WebMvcTest(controllers = AiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ProductAdviceService productAdviceService;

    @MockBean
    BehaviorRecommendationService behaviorRecommendationService;

    @Test
    @DisplayName("POST /api/v1/ai/product-advice → 200 ve gövde")
    void postProductAdvice_ok() throws Exception {
        UUID productId = UUID.randomUUID();

        ProductAdviceResponse resp = new ProductAdviceResponse();
        resp.setVerdict("RECOMMENDED");
        resp.setSummary("Uygun");
        resp.setUsedReviewCount(0);

        given(productAdviceService.getAdvice(any(ProductAdviceRequest.class))).willReturn(resp);

        ProductAdviceRequest body = new ProductAdviceRequest();
        body.setProductId(productId);
        body.setQuestion("Alınır mı?");

        mockMvc.perform(post("/api/v1/ai/product-advice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verdict").value("RECOMMENDED"))
                .andExpect(jsonPath("$.data.summary").value("Uygun"))
                .andExpect(jsonPath("$.data.usedReviewCount").value(0));
    }

    @Test
    @DisplayName("POST /api/v1/ai/behavior-recommendations → 200")
    void postBehaviorRecommendations_ok() throws Exception {
        UUID uid = UUID.randomUUID();

        BehaviorRecommendationResponse resp = new BehaviorRecommendationResponse();
        resp.setSummary("Özet");
        resp.setConfidence(0.8);
        resp.setRecommendedProducts(List.of());

        given(behaviorRecommendationService.recommend(any(BehaviorRecommendationRequest.class))).willReturn(resp);

        BehaviorRecommendationRequest body = new BehaviorRecommendationRequest();
        body.setUserId(uid);
        body.setLimit(10);

        mockMvc.perform(post("/api/v1/ai/behavior-recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary").value("Özet"))
                .andExpect(jsonPath("$.data.confidence").value(0.8));
    }

    @Test
    @DisplayName("productId eksik → 400")
    void postProductAdvice_missingProductId_badRequest() throws Exception {
        mockMvc.perform(post("/api/v1/ai/product-advice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }
}
