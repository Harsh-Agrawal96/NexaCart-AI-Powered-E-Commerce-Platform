package com.n11bootcamp.payment_service.controller;

import com.n11bootcamp.common.response.ApiResponse;
import com.n11bootcamp.payment_service.client.PaymentResult;
import com.n11bootcamp.payment_service.dto.PaymentRequestDto;
import com.n11bootcamp.payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Tag(name = "Ödemeler", description = "Ödeme işlemleri")
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Ödeme işlemini başlat")
    public ResponseEntity<ApiResponse<PaymentResult>> processPayment(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody PaymentRequestDto dto) {
            
        log.info("[PAYMENT] Request received — userId={}, orderId={}, provider={}",
                userId, dto.getOrderId(), dto.getProviderType());
            
        PaymentResult result = paymentService.processPayment(userId, dto);
            
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.ok(result));
        }
    
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(ApiResponse.<PaymentResult>builder()
                        .success(false)
                        .data(result)
                        .timestamp(OffsetDateTime.now())
                        .build());
    }
}
