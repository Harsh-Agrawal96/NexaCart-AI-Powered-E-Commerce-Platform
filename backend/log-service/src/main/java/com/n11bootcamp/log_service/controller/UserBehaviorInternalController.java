package com.n11bootcamp.log_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.n11bootcamp.log_service.dto.response.UserBehaviorRecentItemResponse;
import com.n11bootcamp.log_service.service.UserBehaviorLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;


@Tag(name = "Internal - Kullanıcı davranışı", description = "Servisler arası")
@Validated
@RestController
@RequestMapping("/internal/user-behavior")
@RequiredArgsConstructor
public class UserBehaviorInternalController {

    private final UserBehaviorLogService userBehaviorLogService;

    @GetMapping("/{userId}/recent")
    @Operation(summary = "Kullanıcının son N davranış kaydı (yeniden eskiye)")
    public ResponseEntity<List<UserBehaviorRecentItemResponse>> getRecent(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        return ResponseEntity.ok(userBehaviorLogService.findRecentByUserId(userId, limit));
    }
}
