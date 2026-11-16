package com.n11bootcamp.user_service.controller;

import com.n11bootcamp.common.response.ApiResponse;
import com.n11bootcamp.user_service.dto.response.UserResponse;
import com.n11bootcamp.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Internal - Kullanıcılar", description = "Servisler arası kullanıcı sorguları")
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalController {

    private final UserService userService;

    @GetMapping("/email/{email}")
    @Operation(summary = "E-posta ile kullanıcı getir")
    public ApiResponse<UserResponse> getByEmail(@PathVariable String email) {
        return ApiResponse.ok(userService.getByEmail(email));
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID ile kullanıcı getir")
    public ApiResponse<UserResponse> getById(@PathVariable UUID id) {
        return ApiResponse.ok(userService.getById(id));
    }
}