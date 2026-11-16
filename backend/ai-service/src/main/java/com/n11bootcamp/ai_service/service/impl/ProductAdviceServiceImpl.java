package com.n11bootcamp.ai_service.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.n11bootcamp.ai_service.config.AiCacheConfiguration;
import com.n11bootcamp.ai_service.client.ProductClient;
import com.n11bootcamp.ai_service.client.ReviewClient;
import com.n11bootcamp.ai_service.dto.ProductResponse;
import com.n11bootcamp.ai_service.dto.ReviewResponse;
import com.n11bootcamp.ai_service.dto.request.ProductAdviceRequest;
import com.n11bootcamp.ai_service.dto.response.ProductAdviceResponse;
import com.n11bootcamp.ai_service.service.GeminiClient;
import com.n11bootcamp.ai_service.service.ProductAdviceService;
import com.n11bootcamp.ai_service.service.parser.ProductAdviceParser;
import com.n11bootcamp.ai_service.service.prompt.ProductAdvicePromptBuilder;
import com.n11bootcamp.common.response.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductAdviceServiceImpl implements ProductAdviceService {

    private final ProductClient productClient;
    private final ReviewClient reviewClient;
    private final ProductAdvicePromptBuilder promptBuilder;
    private final GeminiClient geminiClient;
    private final ProductAdviceParser parser;

    @Override
    @Cacheable(cacheNames = AiCacheConfiguration.PRODUCT_ADVICE_CACHE, keyGenerator = "productAdviceCacheKeyGenerator")
    public ProductAdviceResponse getAdvice(ProductAdviceRequest req) {
        UUID productId = req.getProductId();

        ProductResponse product = productClient.getProductById(productId).getData();

        PageResponse<ReviewResponse> page = reviewClient.listByProduct(
                productId,
                0,
                10,
                "createdAt,desc",
                null,
                null
        ).getData();

        List<ReviewResponse> reviews = page != null && page.getContent() != null
                ? page.getContent()
                : Collections.emptyList();

        List<ReviewResponse> used = reviews.size() > 5 ? reviews.subList(0, 5) : reviews;

        String prompt = promptBuilder.build(product, used, req.getQuestion());
        String raw = geminiClient.generateText(prompt);

        ProductAdviceResponse out = parser.parse(raw);
        out.setUsedReviewCount(used.size());
        return out;
    }
}
