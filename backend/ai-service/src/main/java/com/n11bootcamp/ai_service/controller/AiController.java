package com.n11bootcamp.ai_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.n11bootcamp.ai_service.dto.request.BehaviorRecommendationRequest;
import com.n11bootcamp.ai_service.dto.request.ProductAdviceRequest;
import com.n11bootcamp.ai_service.dto.response.BehaviorRecommendationResponse;
import com.n11bootcamp.ai_service.dto.response.ProductAdviceResponse;
import com.n11bootcamp.ai_service.service.BehaviorRecommendationService;
import com.n11bootcamp.ai_service.service.ProductAdviceService;
import com.n11bootcamp.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "Yapay zekâ", description = "Ürün tavsiyesi ve Gemini tabanlı içerik üretimi")
public class AiController {

    private final ProductAdviceService productAdviceService;
    private final BehaviorRecommendationService behaviorRecommendationService;

    @PostMapping("/product-advice")
    @Operation(summary = "Ürün için alınır mı tavsiyesi (yorumlar + ürün özeti)")
    public ResponseEntity<ApiResponse<ProductAdviceResponse>> productAdvice(
            @Valid @RequestBody ProductAdviceRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.ok(productAdviceService.getAdvice(req)));
    }

    @PostMapping("/behavior-recommendations")
    @Operation(summary = "Kullanıcı davranış geçmişine göre ürün önerileri (Gemini)")
    public ResponseEntity<ApiResponse<BehaviorRecommendationResponse>> behaviorRecommendations(
            @Valid @RequestBody BehaviorRecommendationRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(behaviorRecommendationService.recommend(req)));
    }
}
