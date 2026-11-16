package com.n11bootcamp.payment_service.service;

import com.n11bootcamp.common.enums.OrderStatus;
import com.n11bootcamp.common.enums.PaymentProviderType;
import com.n11bootcamp.payment_service.client.PaymentProvider;
import com.n11bootcamp.payment_service.client.PaymentProviderFactory;
import com.n11bootcamp.payment_service.client.PaymentRequest;
import com.n11bootcamp.payment_service.client.PaymentResult;
import com.n11bootcamp.payment_service.client.feign.OrderClient;
import com.n11bootcamp.payment_service.dto.OrderResponse;
import com.n11bootcamp.payment_service.dto.PaymentRequestDto;
import com.n11bootcamp.payment_service.mapper.PaymentRequestMapper;
import com.n11bootcamp.payment_service.publisher.PaymentEventPublisher;
import com.n11bootcamp.payment_service.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    PaymentProviderFactory providerFactory;

    @Mock
    PaymentRequestMapper requestMapper;

    @Mock
    PaymentEventPublisher eventPublisher;

    @Mock
    OrderClient orderClient;

    @Mock
    PaymentProvider provider;

    @InjectMocks
    PaymentServiceImpl paymentService;

    @Test
    @DisplayName("N11BOOTCAMP provider başarılı: PaymentSucceededEvent publish edilir")
    void processPayment_whenProviderSuccess_shouldPublishSuccessEvent() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        PaymentRequestDto dto = buildDto(orderId, PaymentProviderType.N11_BOOTCAMP_PAY);

        OrderResponse order = buildOrder(orderId, OrderStatus.AWAITING_PAYMENT, BigDecimal.valueOf(100));
        given(orderClient.getOrder(orderId)).willReturn(order);

        PaymentRequest request = PaymentRequest.builder()
                .orderId(orderId)
                .userId(userId)
                .grandTotal(order.getGrandTotal())
                .items(order.getItems())
                .providerType(PaymentProviderType.N11_BOOTCAMP_PAY)
                .build();

        PaymentResult successResult = PaymentResult.builder()
                .success(true)
                .transactionId("tx-123")
                .build();

        given(requestMapper.toPaymentRequest(userId, dto, order)).willReturn(request);
        given(providerFactory.getProvider(PaymentProviderType.N11_BOOTCAMP_PAY)).willReturn(provider);
        given(provider.charge(request)).willReturn(successResult);

        // Act
        PaymentResult result = paymentService.processPayment(userId, dto);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTransactionId()).isEqualTo("tx-123");

        then(orderClient).should().getOrder(orderId);
        then(eventPublisher).should().publishSuccess(request, successResult);
    }

    @Test
    @DisplayName("ALWAYS_FAIL provider başarısız: PaymentFailedEvent publish edilir")
    void processPayment_whenProviderFails_shouldPublishFailEvent() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        PaymentRequestDto dto = buildDto(orderId, PaymentProviderType.ALWAYS_FAIL_PAY);

        OrderResponse order = buildOrder(orderId, OrderStatus.AWAITING_PAYMENT, BigDecimal.valueOf(100));
        given(orderClient.getOrder(orderId)).willReturn(order);

        PaymentRequest request = PaymentRequest.builder()
                .orderId(orderId)
                .userId(userId)
                .grandTotal(order.getGrandTotal())
                .items(order.getItems())
                .providerType(PaymentProviderType.ALWAYS_FAIL_PAY)
                .build();

        PaymentResult failResult = PaymentResult.builder()
                .success(false)
                .failReason("Kart reddedildi")
                .build();

        given(requestMapper.toPaymentRequest(userId, dto, order)).willReturn(request);
        given(providerFactory.getProvider(PaymentProviderType.ALWAYS_FAIL_PAY)).willReturn(provider);
        given(provider.charge(request)).willReturn(failResult);

        // Act
        PaymentResult result = paymentService.processPayment(userId, dto);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getFailReason()).isEqualTo("Kart reddedildi");

        then(orderClient).should().getOrder(orderId);
        then(eventPublisher).should().publishFail(request, failResult);
    }

    @Test
    @DisplayName("AWAITING_PAYMENT dışı sipariş: success=false sonuç döner, provider çağrılmaz")
    void processPayment_whenOrderNotAwaitingPayment_shouldReturnFailureResultWithoutCallingProvider() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        PaymentRequestDto dto = buildDto(orderId, PaymentProviderType.N11_BOOTCAMP_PAY);

        OrderResponse order = buildOrder(orderId, OrderStatus.PAID, BigDecimal.valueOf(100));
        given(orderClient.getOrder(orderId)).willReturn(order);

        // Act
        PaymentResult result = paymentService.processPayment(userId, dto);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getFailReason()).contains("Sipariş ödeme bekliyor durumunda değil");
        then(orderClient).should().getOrder(orderId);
        then(eventPublisher).shouldHaveNoInteractions();
        then(providerFactory).shouldHaveNoInteractions();
        then(provider).shouldHaveNoInteractions();
    }

    private static PaymentRequestDto buildDto(UUID orderId, PaymentProviderType providerType) {
        PaymentRequestDto dto = new PaymentRequestDto();
        ReflectionTestUtils.setField(dto, "orderId", orderId);
        ReflectionTestUtils.setField(dto, "providerType", providerType);
        return dto;
    }

    private static OrderResponse buildOrder(UUID orderId, OrderStatus status, BigDecimal grandTotal) {
        OrderResponse order = new OrderResponse();
        ReflectionTestUtils.setField(order, "id", orderId);
        ReflectionTestUtils.setField(order, "status", status);
        ReflectionTestUtils.setField(order, "grandTotal", grandTotal);
        ReflectionTestUtils.setField(order, "items", List.of());
        return order;
    }
}

