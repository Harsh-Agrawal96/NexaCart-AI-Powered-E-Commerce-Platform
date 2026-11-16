package com.n11bootcamp.order_service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bootcamp.common.enums.OrderStatus;
import com.n11bootcamp.order_service.client.CartServiceHelper;
import com.n11bootcamp.order_service.dto.response.CartItemResponse;
import com.n11bootcamp.order_service.dto.response.CartResponse;
import com.n11bootcamp.order_service.publisher.OrderEventPublisher;
import com.n11bootcamp.order_service.repository.OrderRepository;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    OrderRepository orderRepository;

    @MockBean
    CartServiceHelper cartServiceHelper;

    @MockBean
    OrderEventPublisher orderEventPublisher;

    @MockBean
    ConnectionFactory connectionFactory;

    @BeforeEach
    void cleanDb() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/orders → 201, sipariş oluşturulur")
    void createOrder_shouldReturn201() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        CartResponse cart = buildCart(userId);
        given(cartServiceHelper.getActiveCart(userId)).willReturn(cart);

        // Act
        String body = mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", userId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("success").asBoolean()).isTrue();
        JsonNode data = root.path("data");
        assertThat(data.path("id").asText()).isNotBlank();
        assertThat(data.path("userId").asText()).isEqualTo(userId.toString());
        assertThat(data.path("status").asText()).isEqualTo(OrderStatus.PENDING.name());
        assertThat(new BigDecimal(data.path("grandTotal").asText()))
                .isEqualByComparingTo(cart.getGrandTotal());

        // DB'ye yazıldı mı?
        assertThat(orderRepository.findAll())
                .anyMatch(o -> o.getUserId().equals(userId));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} olmayan → 404")
    void getOrder_whenNotFound_shouldReturn404() throws Exception {
        // Arrange
        UUID missingId = UUID.randomUUID();

        // Act
        String body = mockMvc.perform(get("/api/v1/orders/{id}", missingId)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("success").asBoolean()).isFalse();
        assertThat(root.path("data").path("status").asInt()).isEqualTo(404);
        assertThat(root.path("data").path("code").asText()).isEqualTo("ORD-404");
    }

    private static CartResponse buildCart(UUID userId) {
        CartItemResponse item1 = new CartItemResponse(
                UUID.randomUUID(),
                "Ürün 1",
                "SKU-1",
                BigDecimal.valueOf(10),
                2,
                BigDecimal.valueOf(20)
        );
        CartItemResponse item2 = new CartItemResponse(
                UUID.randomUUID(),
                "Ürün 2",
                "SKU-2",
                BigDecimal.valueOf(15),
                1,
                BigDecimal.valueOf(15)
        );

        CartResponse cart = new CartResponse();
        cart.setCartId(UUID.randomUUID());
        cart.setUserId(userId);
        cart.setItems(List.of(item1, item2));
        cart.setGrandTotal(BigDecimal.valueOf(35));
        return cart;
    }
}

