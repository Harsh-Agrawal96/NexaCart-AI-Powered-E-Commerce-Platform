package com.n11bootcamp.comment_service.controller;

import com.n11bootcamp.comment_service.dto.request.CreateReviewRequest;
import com.n11bootcamp.comment_service.dto.request.UpdateReviewRequest;
import com.n11bootcamp.comment_service.dto.response.ReviewResponse;
import com.n11bootcamp.comment_service.service.ProductReviewService;
import com.n11bootcamp.common.constant.HeaderConstants;
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
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Yorumlar", description = "Ürün yorum/rating işlemleri")
public class ProductReviewController {

    private final ProductReviewService productReviewService;

    @PostMapping
    @Operation(summary = "Ürüne yorum ve puan ver")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
            @RequestHeader(HeaderConstants.USER_ID) UUID userId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(productReviewService.create(userId, request)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Kendi yorumunu güncelle")
    public ResponseEntity<ApiResponse<ReviewResponse>> update(
            @PathVariable UUID id,
            @RequestHeader(HeaderConstants.USER_ID) UUID userId,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(productReviewService.update(id, userId, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Kendi yorumunu sil (soft delete)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @RequestHeader(HeaderConstants.USER_ID) UUID userId
    ) {
        productReviewService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping
    @Operation(summary = "Ürüne ait yorumları listele")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> listByProduct(
            @RequestParam UUID productId,
            @RequestParam(required = false) Integer ratingMin,
            @RequestParam(required = false) Integer ratingMax,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(productReviewService.listByProduct(productId, ratingMin, ratingMax, pageable)));
    }
}

