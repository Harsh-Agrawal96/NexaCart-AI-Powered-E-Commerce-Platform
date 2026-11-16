package com.n11bootcamp.product_service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bootcamp.product_service.entity.Category;
import com.n11bootcamp.product_service.entity.Product;
import com.n11bootcamp.product_service.repository.CategoryRepository;
import com.n11bootcamp.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @MockBean
    ConnectionFactory connectionFactory;

    @MockBean
    RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = Category.builder()
                .name("Elektronik")
                .slug("elektronik")
                .active(true)
                .build();
        category = categoryRepository.saveAndFlush(category);

        // En az 6 ürün seed → size=5 testi için
        for (int i = 1; i <= 6; i++) {
            Product p = Product.builder()
                    .name("Ürün " + i)
                    .description("desc")
                    .sku("SKU-" + i)
                    .price(BigDecimal.valueOf(10 + i))
                    .quantity(100)
                    .reservedQuantity(0)
                    .active(true)
                    .category(category)
                    .build();
            productRepository.saveAndFlush(p);
        }
    }

    private String testUserId() {
        return UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("GET /api/v1/products → 200, pagination var")
    void getAll_shouldReturn200AndPageStructure() throws Exception {
        // Act
        String body = mockMvc.perform(get("/api/v1/products")
                        .header("X-User-Id", testUserId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("success").asBoolean()).isTrue();

        JsonNode data = root.path("data");
        assertThat(data.isMissingNode()).isFalse();
        assertThat(data.path("content").isArray()).isTrue();
        assertThat(data.path("totalElements").isNumber()).isTrue();
    }

    @Test
    @DisplayName("GET /api/v1/products?page=0&size=5 → 5 ürün")
    void getAll_withPageAndSize_shouldReturnFiveItems() throws Exception {
        // Act
        String body = mockMvc.perform(get("/api/v1/products")
                        .queryParam("page", "0")
                        .queryParam("size", "5")
                        .header("X-User-Id", testUserId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        JsonNode root = objectMapper.readTree(body);
        JsonNode content = root.path("data").path("content");
        assertThat(content.isArray()).isTrue();
        assertThat(content.size()).isEqualTo(5);
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} olmayan → 404")
    void getById_whenNotFound_shouldReturn404() throws Exception {
        // Arrange
        UUID missingId = UUID.randomUUID();

        // Act
        String body = mockMvc.perform(get("/api/v1/products/{id}", missingId)
                        .header("X-User-Id", testUserId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("success").asBoolean()).isFalse();
        assertThat(root.path("data").path("status").asInt()).isEqualTo(404);
        assertThat(root.path("data").path("code").asText()).isEqualTo("PRD-404");
    }
}

