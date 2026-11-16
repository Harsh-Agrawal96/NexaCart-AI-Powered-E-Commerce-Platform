package com.n11bootcamp.order_service.service.impl;

import com.n11bootcamp.common.enums.OrderStatus;
import com.n11bootcamp.common.event.OrderCreatedEvent;
import com.n11bootcamp.common.exception.BusinessException;
import com.n11bootcamp.common.response.PageResponse;
import com.n11bootcamp.order_service.client.CartServiceHelper;
import com.n11bootcamp.order_service.dto.response.CartResponse;
import com.n11bootcamp.order_service.dto.response.OrderResponse;
import com.n11bootcamp.order_service.dto.response.OrderSummaryResponse;
import com.n11bootcamp.order_service.entity.Order;
import com.n11bootcamp.order_service.exception.OrderErrorCode;
import com.n11bootcamp.order_service.mapper.OrderMapper;
import com.n11bootcamp.order_service.publisher.OrderEventPublisher;
import com.n11bootcamp.order_service.repository.OrderRepository;
import com.n11bootcamp.order_service.service.OrderService;
import com.n11bootcamp.order_service.validator.OrderValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.n11bootcamp.order_service.specification.OrderSpecification.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderValidator orderValidator;
    private final OrderEventPublisher eventPublisher;
    private final CartServiceHelper cartServiceHelper;

    @Override
    @Transactional
    public OrderResponse createOrder(UUID userId) {
        log.info("Creating order for userId: {}", userId);

        CartResponse cart = cartServiceHelper.getActiveCart(userId);

        Order order = orderMapper.toEntity(userId, cart);
        order = orderRepository.save(order);

        OrderCreatedEvent event = orderMapper.toOrderCreatedEvent(order);

        eventPublisher.publishOrderCreated(event);
        log.info("OrderCreatedEvent published for orderId: {}", order.getId());

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public void cancelOrder(UUID orderId, String reason) {
        log.info("Cancelling order: {}, reason: {}", orderId, reason);

        Order order = findOrderById(orderId);
        orderValidator.validateCancellable(order);

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        orderRepository.save(order);

        log.info("Order cancelled: {}", orderId);
    }

    @Override
    @Transactional
    public void awaitPayment(UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        order.setStatus(OrderStatus.AWAITING_PAYMENT);
        orderRepository.save(order);
    }
    
    @Override
    @Transactional
    public void completeOrder(UUID orderId) {
        log.info("Completing order: {}", orderId);

        Order order = findOrderById(orderId);
        orderValidator.validateCompletable(order);

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        log.info("Order completed: {}", orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        log.info("Fetching order: {}", orderId);
        Order order = findOrderById(orderId);
        return orderMapper.toResponse(order);
    }

    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    // ─── Admin ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> searchForAdmin(
            OrderStatus status,
            UUID userId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable) {

        Specification<Order> spec = Specification
                .where(isNotDeleted())
                .and(hasStatus(status))
                .and(hasUserId(userId))
                .and(createdAtFrom(createdFrom))
                .and(createdAtTo(createdTo));

        return PageResponse.of(
                orderRepository.findAll(spec, pageable).map(orderMapper::toSummaryResponse));
    }
}