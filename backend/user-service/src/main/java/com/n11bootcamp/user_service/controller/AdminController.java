package com.n11bootcamp.user_service.controller;

import com.n11bootcamp.common.response.ApiResponse;
import com.n11bootcamp.user_service.dto.response.UserResponse;
import com.n11bootcamp.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin - Kullanıcılar", description = "Kullanıcı yönetimi (admin)")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/{id}/deleted")
    @Operation(summary = "Silinmiş kullanıcıyı getir")
    public ResponseEntity<ApiResponse<UserResponse>> getDeletedUser(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getDeletedById(id)));
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Silinmiş kullanıcıyı geri yükle")
    public ResponseEntity<ApiResponse<UserResponse>> restoreUser(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.restore(id)));
    }

}