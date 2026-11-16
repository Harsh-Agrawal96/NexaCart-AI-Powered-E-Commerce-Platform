package com.n11bootcamp.payment_service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bootcamp.common.enums.OrderStatus;
import com.n11bootcamp.common.enums.PaymentProviderType;
import com.n11bootcamp.common.event.OrderItemPayload;
import com.n11bootcamp.payment_service.client.PaymentProvider;
import com.n11bootcamp.payment_service.client.PaymentProviderFactory;
import com.n11bootcamp.payment_service.client.PaymentRequest;
import com.n11bootcamp.payment_service.client.PaymentResult;
import com.n11bootcamp.payment_service.client.feign.OrderClient;
import com.n11bootcamp.payment_service.dto.OrderResponse;
import com.n11bootcamp.payment_service.publisher.PaymentEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    OrderClient orderClient;

    @MockBean
    PaymentProviderFactory providerFactory;

    @MockBean
    PaymentEventPublisher eventPublisher;

    @MockBean
    ConnectionFactory connectionFactory;

    @Test
    @DisplayName("POST /api/v1/payment success → 200, ApiResponse.success=true")
    void processPayment_whenSuccess_shouldReturn200() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OrderResponse order = buildOrder(orderId, OrderStatus.AWAITING_PAYMENT, BigDecimal.valueOf(100));
        given(orderClient.getOrder(orderId)).willReturn(order);

        PaymentProvider provider = mock(PaymentProvider.class);
        given(providerFactory.getProvider(PaymentProviderType.N11_BOOTCAMP_PAY)).willReturn(provider);
        given(provider.charge(any(PaymentRequest.class))).willReturn(PaymentResult.builder()
                .success(true)
                .transactionId("tx-1")
                .build());

        String json = """
                {
                  "orderId": "%s",
                  "providerType": "N11_BOOTCAMP_PAY",
                  "cardHolderName": "Test",
                  "cardNumber": "5528790000000008",
                  "expireMonth": "12",
                  "expireYear": "2030",
                  "cvc": "123",
                  "buyerName": "Ada",
                  "buyerSurname": "Lovelace",
                  "buyerEmail": "ada@n11.com",
                  "buyerPhone": "+905551112233",
                  "buyerCity": "Istanbul",
                  "buyerAddress": "Address"
                }
                """.formatted(orderId);

        // Act
        String body = mockMvc.perform(post("/api/v1/payment")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("success").asBoolean()).isTrue();
        assertThat(root.path("data").path("success").asBoolean()).isTrue();
        assertThat(root.path("data").path("transactionId").asText()).isEqualTo("tx-1");
    }

    @Test
    @DisplayName("POST /api/v1/payment fail → 402, ApiResponse.success=false")
    void processPayment_whenFail_shouldReturn402() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OrderResponse order = buildOrder(orderId, OrderStatus.AWAITING_PAYMENT, BigDecimal.valueOf(100));
        given(orderClient.getOrder(orderId)).willReturn(order);

        PaymentProvider provider = mock(PaymentProvider.class);
        given(providerFactory.getProvider(PaymentProviderType.ALWAYS_FAIL_PAY)).willReturn(provider);
        given(provider.charge(any(PaymentRequest.class))).willReturn(PaymentResult.builder()
                .success(false)
                .failReason("Kart reddedildi")
                .build());

        String json = """
                {"orderId":"%s","providerType":"ALWAYS_FAIL_PAY"}
                """.formatted(orderId);

        // Act
        String body = mockMvc.perform(post("/api/v1/payment")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isPaymentRequired())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("success").asBoolean()).isFalse();
        assertThat(root.path("data").path("success").asBoolean()).isFalse();
        assertThat(root.path("data").path("failReason").asText()).isEqualTo("Kart reddedildi");
    }

    @Test
    @DisplayName("POST /api/v1/payment AWAITING_PAYMENT değil → 402, provider çağrılmaz")
    void processPayment_whenOrderNotAwaitingPayment_shouldReturn402AndNotCallProvider() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        OrderResponse order = buildOrder(orderId, OrderStatus.PAID, BigDecimal.valueOf(100));
        given(orderClient.getOrder(orderId)).willReturn(order);

        String json = """
                {"orderId":"%s","providerType":"N11_BOOTCAMP_PAY"}
                """.formatted(orderId);

        // Act
        String body = mockMvc.perform(post("/api/v1/payment")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isPaymentRequired())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("success").asBoolean()).isFalse();
        assertThat(root.path("data").path("success").asBoolean()).isFalse();

        then(providerFactory).shouldHaveNoInteractions();
        then(eventPublisher).shouldHaveNoInteractions();
    }

    private static OrderResponse buildOrder(UUID orderId, OrderStatus status, BigDecimal total) {
        OrderResponse order = new OrderResponse();
        ReflectionTestUtils.setField(order, "id", orderId);
        ReflectionTestUtils.setField(order, "status", status);
        ReflectionTestUtils.setField(order, "grandTotal", total);
        ReflectionTestUtils.setField(order, "items", List.of(
                OrderItemPayload.builder()
                        .productId(UUID.randomUUID())
                        .amount(1)
                        .unitPrice(total)
                        .build()
        ));
        return order;
    }
}

