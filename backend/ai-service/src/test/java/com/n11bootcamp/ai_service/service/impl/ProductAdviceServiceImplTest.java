package com.n11bootcamp.ai_service.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.n11bootcamp.ai_service.client.ProductClient;
import com.n11bootcamp.ai_service.client.ReviewClient;
import com.n11bootcamp.ai_service.dto.ProductResponse;
import com.n11bootcamp.ai_service.dto.ReviewResponse;
import com.n11bootcamp.ai_service.dto.request.ProductAdviceRequest;
import com.n11bootcamp.ai_service.dto.response.ProductAdviceResponse;
import com.n11bootcamp.ai_service.service.GeminiClient;
import com.n11bootcamp.ai_service.service.parser.ProductAdviceParser;
import com.n11bootcamp.ai_service.service.prompt.ProductAdvicePromptBuilder;
import com.n11bootcamp.common.response.ApiResponse;
import com.n11bootcamp.common.response.PageResponse;

@ExtendWith(MockitoExtension.class)
class ProductAdviceServiceImplTest {

    @Mock
    ProductClient productClient;
    @Mock
    ReviewClient reviewClient;
    @Mock
    ProductAdvicePromptBuilder promptBuilder;
    @Mock
    GeminiClient geminiClient;
    @Mock
    ProductAdviceParser parser;

    @InjectMocks
    ProductAdviceServiceImpl service;

    @Test
    @DisplayName("getAdvice en fazla 5 yorum kullanır ve usedReviewCount set eder")
    void getAdvice_capsReviewsAtFive_setsUsedCount() {
        UUID productId = UUID.randomUUID();
        ProductAdviceRequest req = new ProductAdviceRequest();
        req.setProductId(productId);
        req.setQuestion("Nasıl?");

        ProductResponse product = new ProductResponse();
        product.setName("Ürün");

        List<ReviewResponse> six = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            ReviewResponse r = new ReviewResponse();
            r.setRating(4);
            r.setComment("yorum " + i);
            six.add(r);
        }

        PageResponse<ReviewResponse> page = PageResponse.<ReviewResponse>builder()
                .content(six)
                .page(0)
                .size(10)
                .totalElements(6)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        given(productClient.getProductById(productId)).willReturn(ApiResponse.ok(product));
        given(reviewClient.listByProduct(productId, 0, 10, "createdAt,desc", null, null))
                .willReturn(ApiResponse.ok(page));
        given(promptBuilder.build(eq(product), eq(six.subList(0, 5)), eq("Nasıl?"))).willReturn("prompt");
        given(geminiClient.generateText("prompt")).willReturn("{}");

        ProductAdviceResponse parsed = new ProductAdviceResponse();
        parsed.setVerdict("MIXED");
        given(parser.parse("{}")).willReturn(parsed);

        ProductAdviceResponse out = service.getAdvice(req);

        assertThat(out.getUsedReviewCount()).isEqualTo(5);
        verify(promptBuilder).build(eq(product), eq(six.subList(0, 5)), eq("Nasıl?"));
    }

    @Test
    @DisplayName("Sayfa veya content null ise yorum listesi boş")
    void getAdvice_nullPage_usesEmptyReviews() {
        UUID productId = UUID.randomUUID();
        ProductAdviceRequest req = new ProductAdviceRequest();
        req.setProductId(productId);
        req.setQuestion(null);

        ProductResponse product = new ProductResponse();

        given(productClient.getProductById(productId)).willReturn(ApiResponse.ok(product));
        given(reviewClient.listByProduct(productId, 0, 10, "createdAt,desc", null, null))
                .willReturn(ApiResponse.ok(null));
        given(promptBuilder.build(eq(product), eq(List.of()), isNull())).willReturn("p");
        given(geminiClient.generateText("p")).willReturn("{}");

        ProductAdviceResponse parsed = new ProductAdviceResponse();
        given(parser.parse("{}")).willReturn(parsed);

        ProductAdviceResponse out = service.getAdvice(req);

        assertThat(out.getUsedReviewCount()).isZero();
        verify(promptBuilder).build(eq(product), eq(List.of()), isNull());
    }
}
