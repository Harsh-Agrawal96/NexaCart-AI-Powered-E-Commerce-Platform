package com.n11bootcamp.cart_service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bootcamp.cart_service.client.ProductServiceHelper;
import com.n11bootcamp.cart_service.client.dto.ProductResponse;
import com.n11bootcamp.cart_service.entity.Cart;
import com.n11bootcamp.cart_service.entity.enums.CartStatus;
import com.n11bootcamp.cart_service.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CartRepository cartRepository;

    @MockBean
    ProductServiceHelper productServiceHelper;

    @MockBean
    ConnectionFactory connectionFactory;

    private UUID userId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();

        ProductResponse product = new ProductResponse(
                productId,
                "Test Ürün",
                "SKU-1",
                BigDecimal.valueOf(99.90),
                true,
                100
        );
        given(productServiceHelper.fetchProductMap(anyList()))
                .willReturn(Map.of(productId, product));
    }

    @Test
    @DisplayName("GET /api/v1/cart → 200, boş sepet döner")
    void getActiveCart_whenNoCart_shouldReturnEmptyCart() throws Exception {
        String body = mockMvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", userId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("success").asBoolean()).isTrue();
        JsonNode data = root.path("data");
        assertThat(data.path("userId").asText()).isEqualTo(userId.toString());
        assertThat(data.path("items").isArray()).isTrue();
        assertThat(data.path("items").size()).isEqualTo(0);
        assertThat(new BigDecimal(data.path("grandTotal").asText()))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("POST /api/v1/cart/items → 200, sepete ürün eklenir")
    void addItem_shouldAddItemToCart() throws Exception {
        String json = """
                {"productId":"%s","amount":2}
                """.formatted(productId);

        String body = mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("success").asBoolean()).isTrue();
        JsonNode items = root.path("data").path("items");
        assertThat(items.isArray()).isTrue();
        assertThat(items.size()).isEqualTo(1);
        assertThat(items.get(0).path("productId").asText()).isEqualTo(productId.toString());
        assertThat(items.get(0).path("amount").asInt()).isEqualTo(2);
    }

    @Test
    @DisplayName("PATCH /api/v1/cart/items/{productId} → 200, miktar güncellenir")
    void updateItemAmount_shouldUpdateAmount() throws Exception {
        // Arrange: önce ekle
        mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","amount":2}
                                """.formatted(productId)))
                .andExpect(status().isOk());

        // Act
        String body = mockMvc.perform(patch("/api/v1/cart/items/{productId}", productId)
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":5}
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        JsonNode root = objectMapper.readTree(body);
        JsonNode items = root.path("data").path("items");
        assertThat(items.size()).isEqualTo(1);
        assertThat(items.get(0).path("amount").asInt()).isEqualTo(5);
    }

    @Test
    @DisplayName("DELETE /api/v1/cart/items/{productId} (item yok) → 404")
    void removeItem_whenItemMissing_shouldReturn404() throws Exception {
        // Arrange: aktif sepet var ama item yok
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setStatus(CartStatus.ACTIVE);
        cartRepository.saveAndFlush(cart);

        // Act
        String body = mockMvc.perform(delete("/api/v1/cart/items/{productId}", productId)
                        .header("X-User-Id", userId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("success").asBoolean()).isFalse();
        assertThat(root.path("data").path("status").asInt()).isEqualTo(404);
        assertThat(root.path("data").path("code").asText()).isEqualTo("CART-404-1");
    }
}

