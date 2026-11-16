package com.n11bootcamp.product_service.service.impl;

import com.n11bootcamp.common.exception.BusinessException;
import com.n11bootcamp.product_service.dto.request.CreateProductRequest;
import com.n11bootcamp.product_service.dto.request.UpdateProductRequest;
import com.n11bootcamp.product_service.dto.response.ProductResponse;
import com.n11bootcamp.product_service.dto.response.ProductSummaryResponse;
import com.n11bootcamp.product_service.dto.response.StockResponse;
import com.n11bootcamp.product_service.entity.Category;
import com.n11bootcamp.product_service.entity.Product;
import com.n11bootcamp.product_service.exception.CategoryErrorCode;
import com.n11bootcamp.product_service.exception.ProductErrorCode;
import com.n11bootcamp.product_service.mapper.ProductMapper;
import com.n11bootcamp.product_service.repository.CategoryRepository;
import com.n11bootcamp.product_service.repository.ProductRepository;
import com.n11bootcamp.product_service.publisher.UserBehaviorEventPublisher;
import com.n11bootcamp.product_service.service.ProductService;
import com.n11bootcamp.product_service.validator.ProductValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
 
import static com.n11bootcamp.product_service.specification.ProductSpecification.*;
 
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
 
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductValidator productValidator;
    private final UserBehaviorEventPublisher userBehaviorEventPublisher;
 
    // ─── Admin ───────────────────────────────────────────────────────────────
 
    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        log.debug("Creating product with sku: {}", request.sku());
 
        productValidator.validateCreate(request);
 
        Category category = findCategoryOrThrow(request.categoryId());
 
        Product product = productMapper.toEntity(request);
        product.setCategory(category);
 
        Product saved = productRepository.saveAndFlush(product);
 
        log.info("Product created with id: {}", saved.getId());
        return productMapper.toResponse(saved);
    }
 
    @Override
    @Transactional
    public ProductResponse update(UUID id, UpdateProductRequest request) {
        log.debug("Updating product: {}", id);
 
        Product product = findByIdOrThrow(id);
 
        if (request.categoryId() != null) {
            Category category = findCategoryOrThrow(request.categoryId());
            product.setCategory(category);
        }
 
        productMapper.updateEntity(request, product);
 
        log.info("Product updated: {}", id);
        return productMapper.toResponse(product);
    }
 
    @Override
    @Transactional
    public void delete(UUID id) {
        log.debug("Soft deleting product: {}", id);
 
        Product product = findByIdOrThrow(id);
        product.delete();
        product.setActive(false);
 
        log.info("Product soft deleted: {}", id);
    }
 
    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> getAllForAdmin(UUID categoryId, String search, Pageable pageable) {
        Specification<Product> spec = Specification
                .where(isNotDeleted())
                .and(hasCategory(categoryId))
                .and(nameLike(search));
 
        return productRepository.findAll(spec, pageable)
                .map(productMapper::toSummaryResponse);
    }
 
    // ─── Public ──────────────────────────────────────────────────────────────
 
    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(UUID id, UUID behaviorUserId) {
        Product product = findByIdOrThrow(id);
        productValidator.validateActive(product);
        ProductResponse response = productMapper.toResponse(product);
        userBehaviorEventPublisher.publishProductViewed(behaviorUserId, id);
        return response;
    }
 
    @Override
    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> getAllActive(UUID categoryId, String search,
                                                     BigDecimal minPrice, BigDecimal maxPrice,
                                                     Pageable pageable) {
        Specification<Product> spec = Specification
                .where(isNotDeleted())
                .and(isActive())
                .and(hasCategory(categoryId))
                .and(nameLike(search))
                .and(minPrice(minPrice))
                .and(maxPrice(maxPrice));
 
        return productRepository.findAll(spec, pageable)
                .map(productMapper::toSummaryResponse);
    }
 
    // ─── Internal ────────────────────────────────────────────────────────────
 
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getActiveProductsByIds(List<UUID> ids) {
        return productRepository.findAllByIdInAndDeletedFalseAndActiveTrue(ids)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional
    public StockResponse decreaseStock(UUID productId, int amount) {
        log.debug("Decreasing stock for product: {}, amount: {}", productId, amount);
 
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
 
        productValidator.validateStock(product, amount);
        product.decreaseStock(amount);
 
        log.info("Stock decreased for product: {} | remaining: {}", productId, product.getQuantity());
        return buildStockResponse(product);
    }
 
    @Override
    @Transactional
    public StockResponse increaseStock(UUID productId, int amount) {
        log.debug("Increasing stock for product: {}, amount: {}", productId, amount);
 
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
 
        if (amount <= 0) {
            throw new BusinessException(ProductErrorCode.INVALID_STOCK_AMOUNT);
        }
 
        product.increaseStock(amount);
 
        log.info("Stock increased for product: {} | remaining: {}", productId, product.getQuantity());
        return buildStockResponse(product);
    }
 
    @Override
    @Transactional(readOnly = true)
    public StockResponse checkStock(UUID productId, int amount) {
        Product product = findByIdOrThrow(productId);
        return new StockResponse(
                product.getId(),
                product.getSku(),
                product.getQuantity(),
                product.getReservedQuantity(),
                product.getAvailableQuantity(),
                product.hasStock(amount)
        );
    }
 
    @Override
    @Transactional
    public StockResponse reserveStock(UUID productId, int amount) {
        log.debug("Reserving stock for product: {}, amount: {}", productId, amount);

        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));

        productValidator.validateStock(product, amount);
        product.reserveStock(amount);

        log.info("Stock reserved for product: {} | reserved: {}", productId, product.getReservedQuantity());
        return buildStockResponse(product);
    }

    @Override
    @Transactional
    public StockResponse releaseReservedStock(UUID productId, int amount) {
        log.debug("Releasing reserved stock for product: {}, amount: {}", productId, amount);

        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));

        product.releaseReservedStock(amount);

        log.info("Stock released for product: {} | reserved: {}", productId, product.getReservedQuantity());
        return buildStockResponse(product);
    }

    @Override
    @Transactional
    public StockResponse confirmStock(UUID productId, int amount) {
        log.debug("Confirming stock for product: {}, amount: {}", productId, amount);

        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));

        product.confirmStock(amount);

        log.info("Stock confirmed for product: {} | remaining: {}", productId, product.getQuantity());
        return buildStockResponse(product);
    }

    // ─── Private ──────────────────────────────────────────────────────────────
 
    private Product findByIdOrThrow(UUID id) {
        return productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }
 
    private Category findCategoryOrThrow(UUID categoryId) {
        return categoryRepository.findByIdAndDeletedFalse(categoryId)
                .orElseThrow(() -> new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND));
    }
 
    private StockResponse buildStockResponse(Product product) {
        return new StockResponse(
                product.getId(),
                product.getSku(),
                product.getQuantity(),
                product.getReservedQuantity(),
                product.getAvailableQuantity(),
                true
        );
    }
}