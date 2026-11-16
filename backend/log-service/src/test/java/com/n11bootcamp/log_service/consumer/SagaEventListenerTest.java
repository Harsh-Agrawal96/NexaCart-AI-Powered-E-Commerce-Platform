package com.n11bootcamp.log_service.consumer;

import com.n11bootcamp.common.event.*;
import com.n11bootcamp.log_service.entity.enums.EventType;
import com.n11bootcamp.log_service.service.SagaLogEntryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SagaEventListenerTest {

    @Mock SagaLogEntryService sagaLogEntryService;
    @InjectMocks SagaEventListener sagaEventListener;

    @Test
    @DisplayName("OrderCreatedEvent gelince: ORDER_CREATED olarak log kaydı yapılır")
    void handleOrderCreated_shouldSaveSagaEvent() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
                .userId(userId)
                .grandTotal(BigDecimal.valueOf(123.45))
                .items(List.of())
                .build();

        // Act
        sagaEventListener.handleOrderCreated(event);

        // Assert
        ArgumentCaptor<EventType> eventType = ArgumentCaptor.forClass(EventType.class);
        ArgumentCaptor<String> serviceName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> orderIdStr = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userIdStr = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);

        then(sagaLogEntryService).should().saveSagaEvent(
                eventType.capture(),
                serviceName.capture(),
                message.capture(),
                orderIdStr.capture(),
                userIdStr.capture(),
                payload.capture()
        );

        assertThat(eventType.getValue()).isEqualTo(EventType.ORDER_CREATED);
        assertThat(serviceName.getValue()).isEqualTo("order-service");
        assertThat(message.getValue()).contains("Order created with total");
        assertThat(orderIdStr.getValue()).isEqualTo(orderId.toString());
        assertThat(userIdStr.getValue()).isEqualTo(userId.toString());
        assertThat(payload.getValue()).isSameAs(event);
    }

    @Test
    @DisplayName("PaymentFailedEvent gelince: PAYMENT_FAILED olarak log kaydı yapılır (userId yok)")
    void handlePaymentFailed_shouldSaveSagaEvent() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .orderId(orderId)
                .reason("Kart reddedildi")
                .items(List.of())
                .build();

        // Act
        sagaEventListener.handlePaymentFailed(event);

        // Assert
        ArgumentCaptor<EventType> eventType = ArgumentCaptor.forClass(EventType.class);
        ArgumentCaptor<String> serviceName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> orderIdStr = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userIdStr = ArgumentCaptor.forClass(String.class);

        then(sagaLogEntryService).should().saveSagaEvent(
                eventType.capture(),
                serviceName.capture(),
                message.capture(),
                orderIdStr.capture(),
                userIdStr.capture(),
                org.mockito.ArgumentMatchers.eq(event)
        );

        assertThat(eventType.getValue()).isEqualTo(EventType.PAYMENT_FAILED);
        assertThat(serviceName.getValue()).isEqualTo("payment-service");
        assertThat(message.getValue()).contains("Payment failed");
        assertThat(orderIdStr.getValue()).isEqualTo(orderId.toString());
        assertThat(userIdStr.getValue()).isNull();
    }
}

