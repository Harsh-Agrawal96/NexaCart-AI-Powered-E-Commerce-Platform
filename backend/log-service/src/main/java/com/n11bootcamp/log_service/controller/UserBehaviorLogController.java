package com.n11bootcamp.log_service.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.n11bootcamp.log_service.entity.UserBehaviorLogEntry;
import com.n11bootcamp.log_service.service.UserBehaviorLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Kullanıcı davranış logları", description = "Admin — ürün görüntüleme / sepet vb. kayıtları")
@RestController
@RequestMapping("/api/v1/logs/user-behavior")
@RequiredArgsConstructor
public class UserBehaviorLogController {

    private final UserBehaviorLogService userBehaviorLogService;

    @GetMapping
    @Operation(summary = "Tüm kullanıcı davranış loglarını listele")
    public ResponseEntity<Page<UserBehaviorLogEntry>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(userBehaviorLogService.getAll(pageable));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Kullanıcıya göre davranış logları")
    public ResponseEntity<Page<UserBehaviorLogEntry>> getByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(userBehaviorLogService.getByUserId(userId, pageable));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Ürüne göre davranış logları")
    public ResponseEntity<Page<UserBehaviorLogEntry>> getByProductId(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(userBehaviorLogService.getByProductId(productId, pageable));
    }
}
