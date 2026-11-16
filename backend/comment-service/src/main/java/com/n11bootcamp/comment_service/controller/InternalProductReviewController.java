package com.n11bootcamp.comment_service.controller;

import com.n11bootcamp.comment_service.dto.response.ReviewResponse;
import com.n11bootcamp.comment_service.service.ProductReviewService;
import com.n11bootcamp.common.response.ApiResponse;
import com.n11bootcamp.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Internal - Yorumlar", description = "Servisler arası yorum/rating sorguları (MCP)")
@RestController
@RequestMapping("/internal/reviews")
@RequiredArgsConstructor
public class InternalProductReviewController {

    private final ProductReviewService productReviewService;

    @GetMapping
    @Operation(summary = "Ürüne ait yorumları getir (internal)")
    public ApiResponse<PageResponse<ReviewResponse>> listByProduct(
            @RequestParam UUID productId,
            @RequestParam(required = false) Integer ratingMin,
            @RequestParam(required = false) Integer ratingMax,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.ok(productReviewService.listByProduct(productId, ratingMin, ratingMax, pageable));
    }
}

