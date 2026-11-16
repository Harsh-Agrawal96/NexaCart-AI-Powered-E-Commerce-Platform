package com.n11bootcamp.log_service.controller;

import com.n11bootcamp.log_service.entity.SagaLogEntry;
import com.n11bootcamp.log_service.service.SagaLogEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Loglar", description = "Log görüntüleme ve sorgulama")
@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class LogController {

    private final SagaLogEntryService sagaLogEntryService;

    @GetMapping
    @Operation(summary = "Tüm logları listele")
    public ResponseEntity<Page<SagaLogEntry>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(sagaLogEntryService.getAllLogs(pageable));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Siparişe göre logları listele")
    public ResponseEntity<Page<SagaLogEntry>> getLogsByOrderId(
            @PathVariable String orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(sagaLogEntryService.getLogsByOrderId(orderId, pageable));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Kullanıcıya göre logları listele")
    public ResponseEntity<Page<SagaLogEntry>> getLogsByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(sagaLogEntryService.getLogsByUserId(userId, pageable));
    }

    @GetMapping("/service/{serviceName}")
    @Operation(summary = "Servise göre logları listele")
    public ResponseEntity<Page<SagaLogEntry>> getLogsByServiceName(
            @PathVariable String serviceName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(sagaLogEntryService.getLogsByServiceName(serviceName, pageable));
    }
}