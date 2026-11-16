package com.n11bootcamp.order_service.service;

import com.n11bootcamp.common.enums.OrderStatus;
import com.n11bootcamp.common.event.OrderCreatedEvent;
import com.n11bootcamp.common.exception.BusinessException;
import com.n11bootcamp.order_service.client.CartServiceHelper;
import com.n11bootcamp.order_service.dto.response.CartItemResponse;
import com.n11bootcamp.order_service.dto.response.CartResponse;
import com.n11bootcamp.order_service.dto.response.OrderResponse;
import com.n11bootcamp.order_service.entity.Order;
import com.n11bootcamp.order_service.exception.OrderErrorCode;
import com.n11bootcamp.order_service.mapper.OrderMapper;
import com.n11bootcamp.order_service.publisher.OrderEventPublisher;
import com.n11bootcamp.order_service.repository.OrderRepository;
import com.n11bootcamp.order_service.service.impl.OrderServiceImpl;
import com.n11bootcamp.order_service.validator.OrderValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderEventPublisher eventPublisher;

    @Mock
    CartServiceHelper cartServiceHelper;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        OrderMapper orderMapper = new OrderMapper();
        OrderValidator orderValidator = new OrderValidator();
        orderService = new OrderServiceImpl(
                orderRepository,
                orderMapper,
                orderValidator,
                eventPublisher,
                cartServiceHelper
        );
    }

    @Test
    @DisplayName("Sipariş oluşturulunca: status PENDING, grandTotal doğru ve OrderCreatedEvent publish edilir")
    void createOrder_whenCalled_shouldCreatePendingOrderAndPublishOrderCreatedEvent() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        CartResponse cart = buildCart(userId);
        given(cartServiceHelper.getActiveCart(userId)).willReturn(cart);

        willAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", orderId);
            return order;
        }).given(orderRepository).save(any(Order.class));

        // Act
        OrderResponse response = orderService.createOrder(userId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(orderId);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getGrandTotal()).isEqualByComparingTo(cart.getGrandTotal());
        assertThat(response.getItems()).hasSize(cart.getItems().size());

        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        then(eventPublisher).should().publishOrderCreated(captor.capture());

        OrderCreatedEvent event = captor.getValue();
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getGrandTotal()).isEqualByComparingTo(cart.getGrandTotal());
        assertThat(event.getItems()).hasSize(cart.getItems().size());
        assertThat(event.getItems().stream().map(i -> i.getProductId()))
                .containsExactlyInAnyOrder(
                        cart.getItems().get(0).getProductId(),
                        cart.getItems().get(1).getProductId()
                );
    }

    @Test
    @DisplayName("inventory.reserved gelince: sipariş AWAITING_PAYMENT olur")
    void awaitPayment_whenCalled_shouldSetStatusAwaitingPayment() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.PENDING);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(orderRepository.save(order)).willReturn(order);

        // Act
        orderService.awaitPayment(orderId);

        // Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.AWAITING_PAYMENT);
    }

    @Test
    @DisplayName("payment.succeeded gelince: sipariş PAID olur")
    void completeOrder_whenCalled_shouldSetStatusPaid() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.AWAITING_PAYMENT);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(orderRepository.save(order)).willReturn(order);

        // Act
        orderService.completeOrder(orderId);

        // Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("PAID siparişe tekrar completeOrder gelince: exception fırlatmaz, status PAID kalır")
    void completeOrder_whenOrderIsAlreadyPaid_shouldKeepStatusPaid() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.PAID);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(orderRepository.save(order)).willReturn(order);

        // Act
        orderService.completeOrder(orderId);

        // Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("payment.failed gelince: sipariş CANCELLED olur")
    void cancelOrder_whenPaymentFailed_shouldSetStatusCancelledAndReason() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.AWAITING_PAYMENT);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(orderRepository.save(order)).willReturn(order);

        // Act
        orderService.cancelOrder(orderId, "payment.failed");

        // Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancelReason()).isEqualTo("payment.failed");
    }

    @Test
    @DisplayName("inventory.failed gelince: sipariş CANCELLED olur")
    void cancelOrder_whenInventoryFailed_shouldSetStatusCancelledAndReason() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.PENDING);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(orderRepository.save(order)).willReturn(order);

        // Act
        orderService.cancelOrder(orderId, "inventory.failed");

        // Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancelReason()).isEqualTo("inventory.failed");
    }

    @Test
    @DisplayName("PAID sipariş iptal edilmeye çalışılırsa: exception fırlatır")
    void cancelOrder_whenOrderIsPaid_shouldThrowBusinessException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        ReflectionTestUtils.setField(order, "id", orderId);
        order.setStatus(OrderStatus.PAID);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // Act + Assert
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, "manual"))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(OrderErrorCode.INVALID_ORDER_STATUS)
                );
    }

    private static CartResponse buildCart(UUID userId) {
        UUID cartId = UUID.randomUUID();

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
        cart.setCartId(cartId);
        cart.setUserId(userId);
        cart.setItems(List.of(item1, item2));
        cart.setGrandTotal(BigDecimal.valueOf(35));
        return cart;
    }
}

