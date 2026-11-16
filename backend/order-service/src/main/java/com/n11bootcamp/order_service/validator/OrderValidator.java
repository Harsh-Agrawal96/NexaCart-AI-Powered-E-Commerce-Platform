package com.n11bootcamp.order_service.validator;

import com.n11bootcamp.common.enums.OrderStatus;
import com.n11bootcamp.common.exception.BusinessException;
import com.n11bootcamp.order_service.entity.Order;
import com.n11bootcamp.order_service.exception.OrderErrorCode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderValidator {

    public void validateCancellable(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("Order already CANCELLED, skipping: {}", order.getId());
            return;
        }
        if (order.getStatus() == OrderStatus.PAID) {
            throw new BusinessException(OrderErrorCode.INVALID_ORDER_STATUS);
        }
    }

    public void validateCompletable(Order order) {
        if (order.getStatus() == OrderStatus.PAID) {
            log.warn("Order already PAID, skipping: {}", order.getId());
            return;
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException(OrderErrorCode.INVALID_ORDER_STATUS);
        }
    }
}