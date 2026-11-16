package com.n11bootcamp.comment_service.controller;

import com.n11bootcamp.comment_service.dto.request.admin.AdminCreateReviewRequest;
import com.n11bootcamp.comment_service.dto.request.admin.AdminUpdateReviewRequest;
import com.n11bootcamp.comment_service.dto.response.ReviewResponse;
import com.n11bootcamp.comment_service.service.ProductReviewService;
import com.n11bootcamp.common.response.ApiResponse;
import com.n11bootcamp.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
@Tag(name = "Admin - Yorumlar", description = "Yorum yönetimi (admin)")
public class AdminProductReviewController {

    private final ProductReviewService productReviewService;

    @PostMapping
    @Operation(summary = "Yorum oluştur (admin)")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(@Valid @RequestBody AdminCreateReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(productReviewService.adminCreate(request)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Yorum güncelle (admin)")
    public ResponseEntity<ApiResponse<ReviewResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AdminUpdateReviewRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(productReviewService.adminUpdate(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Herhangi bir yorumu sil (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        productReviewService.adminDelete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Yorum detayı (admin)")
    public ResponseEntity<ApiResponse<ReviewResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(productReviewService.adminGetById(id)));
    }

    @GetMapping
    @Operation(summary = "Yorumları listele/filtrele (admin)", description = "Opsiyonel: productId, userId")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> search(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) Integer ratingMin,
            @RequestParam(required = false) Integer ratingMax,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(productReviewService.adminSearch(productId, userId, ratingMin, ratingMax, pageable)));
    }
}

