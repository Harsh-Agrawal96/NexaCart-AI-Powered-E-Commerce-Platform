package com.n11bootcamp.product_service.service;

import com.n11bootcamp.product_service.dto.request.CreateProductRequest;
import com.n11bootcamp.product_service.dto.request.UpdateProductRequest;
import com.n11bootcamp.product_service.dto.response.ProductResponse;
import com.n11bootcamp.product_service.dto.response.ProductSummaryResponse;
import com.n11bootcamp.product_service.dto.response.StockResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
 
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
 
public interface ProductService {
 
    // ─── Admin ───────────────────────────────────────────────────────────────
    ProductResponse create(CreateProductRequest request);
 
    ProductResponse update(UUID id, UpdateProductRequest request);
 
    void delete(UUID id);
 
    Page<ProductSummaryResponse> getAllForAdmin(UUID categoryId, String search, Pageable pageable);
 
    ProductResponse getById(UUID id, UUID behaviorUserId);
 
    Page<ProductSummaryResponse> getAllActive(UUID categoryId, String search,
                                             BigDecimal minPrice, BigDecimal maxPrice,
                                             Pageable pageable);
 
    // ─── Internal (order/cart-service için) ──────────────────────────────────
    StockResponse decreaseStock(UUID productId, int amount);
 
    StockResponse increaseStock(UUID productId, int amount);
 
    StockResponse checkStock(UUID productId, int amount);

    StockResponse reserveStock(UUID productId, int amount);

    StockResponse releaseReservedStock(UUID productId, int amount);
        
    StockResponse confirmStock(UUID productId, int amount);

    List<ProductResponse> getActiveProductsByIds(List<UUID> ids);

}