package com.n11bootcamp.comment_service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bootcamp.comment_service.client.UserServiceClient;
import com.n11bootcamp.comment_service.client.dto.UserResponse;
import com.n11bootcamp.comment_service.entity.ProductReview;
import com.n11bootcamp.comment_service.repository.ProductReviewRepository;
import com.n11bootcamp.common.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductReviewControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductReviewRepository productReviewRepository;

    @MockBean
    UserServiceClient userServiceClient;

    private UUID productId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        productReviewRepository.deleteAll();
        productId = UUID.randomUUID();
        userId = UUID.randomUUID();

        given(userServiceClient.getById(userId))
                .willReturn(ApiResponse.ok(new UserResponse(userId, "Test", "User", "test@example.com")));

        productReviewRepository.saveAndFlush(ProductReview.builder()
                .productId(productId)
                .userId(userId)
                .rating(5)
                .comment("mükemmel")
                .build());

        productReviewRepository.saveAndFlush(ProductReview.builder()
                .productId(productId)
                .userId(UUID.randomUUID())
                .rating(2)
                .comment("kötü")
                .build());

        productReviewRepository.saveAndFlush(ProductReview.builder()
                .productId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .rating(4)
                .comment("başka ürün")
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/reviews?productId=... → 200, PageResponse yapısı döner")
    void listByProduct_shouldReturnPageResponse() throws Exception {
        String body = mockMvc.perform(get("/api/v1/reviews")
                        .queryParam("productId", productId.toString())
                        .header("X-User-Id", userId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("success").asBoolean()).isTrue();
        JsonNode data = root.path("data");
        assertThat(data.path("content").isArray()).isTrue();
        assertThat(data.path("totalElements").isNumber()).isTrue();
    }

    @Test
    @DisplayName("GET /api/v1/reviews?ratingMin=4&ratingMax=5 → sadece yüksek rating'ler gelir")
    void listByProduct_withRatingRange_shouldFilter() throws Exception {
        String body = mockMvc.perform(get("/api/v1/reviews")
                        .queryParam("productId", productId.toString())
                        .queryParam("ratingMin", "4")
                        .queryParam("ratingMax", "5")
                        .header("X-User-Id", userId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode content = objectMapper.readTree(body).path("data").path("content");
        assertThat(content.size()).isEqualTo(1);
        assertThat(content.get(0).path("rating").asInt()).isEqualTo(5);
    }
}

