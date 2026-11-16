package com.n11bootcamp.payment_service.client.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.n11bootcamp.payment_service.dto.OrderResponse;

import java.util.UUID;

@FeignClient(name = "order-service", path = "/internal/orders")
public interface OrderClient {

    @GetMapping("/{id}")
    OrderResponse getOrder(@PathVariable UUID id);
}
