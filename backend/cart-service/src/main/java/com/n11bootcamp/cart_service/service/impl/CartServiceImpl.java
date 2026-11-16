package com.n11bootcamp.cart_service.service.impl;

import com.n11bootcamp.cart_service.client.ProductServiceHelper;
import com.n11bootcamp.cart_service.client.dto.ProductResponse;
import com.n11bootcamp.cart_service.dto.request.AddItemRequest;
import com.n11bootcamp.cart_service.dto.response.CartResponse;
import com.n11bootcamp.cart_service.dto.response.CartSummaryResponse;
import com.n11bootcamp.cart_service.entity.Cart;
import com.n11bootcamp.cart_service.entity.CartItem;
import com.n11bootcamp.cart_service.entity.enums.CartStatus;
import com.n11bootcamp.cart_service.mapper.CartMapper;
import com.n11bootcamp.cart_service.publisher.UserBehaviorEventPublisher;
import com.n11bootcamp.cart_service.repository.CartItemRepository;
import com.n11bootcamp.cart_service.repository.CartRepository;
import com.n11bootcamp.cart_service.service.CartService;
import com.n11bootcamp.cart_service.validator.CartValidator;
import com.n11bootcamp.common.exception.BusinessException;
import com.n11bootcamp.common.response.PageResponse;
import com.n11bootcamp.cart_service.exception.CartErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.n11bootcamp.cart_service.specification.CartSpecification.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceHelper productServiceHelper;
    private final CartValidator cartValidator;
    private final CartMapper cartMapper;
    private final UserBehaviorEventPublisher userBehaviorEventPublisher;

    @Override
    @Transactional
    public CartResponse addItem(UUID userId, AddItemRequest request) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> createNewCart(userId));

        Map<UUID, ProductResponse> productMap =
                productServiceHelper.fetchProductMap(List.of(request.productId()));

        ProductResponse product =
                cartValidator.requireProductInBatch(productMap, request.productId());

        cartValidator.validateProductActive(product);
        cartValidator.validateStock(product, request.amount());

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), request.productId())
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProductId(request.productId());
                    newItem.setAmount(0);
                    cart.getItems().add(newItem);
                    return newItem;
                });

        cartItem.setAmount(cartItem.getAmount() + request.amount());
        cartRepository.save(cart);

        userBehaviorEventPublisher.publishAddToCart(userId, request.productId());

        log.info("Item added to cart. userId={}, productId={}, amount={}",
                userId, request.productId(), request.amount());

        return getActiveCart(userId);
    }

    @Override
    @Transactional
    public void removeItem(UUID userId, UUID productId) {
        Cart cart = cartValidator.requireActiveCart(
                cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE), userId);

        CartItem cartItem = cartValidator.requireCartItem(
                cartItemRepository.findByCartIdAndProductId(cart.getId(), productId), productId);

        cart.getItems().remove(cartItem);
        cartRepository.save(cart);

        userBehaviorEventPublisher.publishRemoveFromCart(userId, productId);

        log.info("Item removed from cart. userId={}, productId={}", userId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getActiveCart(UUID userId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElse(null);

        if (cart == null || cart.getItems().isEmpty()) {
            return cartMapper.toEmptyCartResponse(cart, userId);
        }

        List<UUID> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .toList();

        Map<UUID, ProductResponse> productMap =
                productServiceHelper.fetchProductMap(productIds);

        return cartMapper.toCartResponse(cart, productMap);
    }

    @Override
        @Transactional
        public void clearCart(UUID userId) {
            cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .ifPresentOrElse(
                    cart -> {
                        cart.setStatus(CartStatus.ORDERED);
                        cartRepository.save(cart);
                        log.info("Cart cleared (ORDERED). userId={}, cartId={}", userId, cart.getId());
                    },
                    () -> log.warn("No ACTIVE cart found for userId: {}, skipping", userId)
                );
        }

    private Cart createNewCart(UUID userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setStatus(CartStatus.ACTIVE);
        return cartRepository.save(cart);
    }

        @Override
        @Transactional
        public CartResponse updateItemAmount(UUID userId, UUID productId, Integer amount) {
            Cart cart = cartValidator.requireActiveCart(
                    cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE), userId);

            CartItem cartItem = cartValidator.requireCartItem(
                    cartItemRepository.findByCartIdAndProductId(cart.getId(), productId), productId);

            Map<UUID, ProductResponse> productMap =
                    productServiceHelper.fetchProductMap(List.of(productId));

            ProductResponse product =
                    cartValidator.requireProductInBatch(productMap, productId);

            cartValidator.validateProductActive(product);
            cartValidator.validateStock(product, amount);

            if (amount == 0) {
                cart.getItems().remove(cartItem);
                userBehaviorEventPublisher.publishRemoveFromCart(userId, productId);
            } else {
                cartItem.setAmount(amount);
            }

            cartRepository.save(cart);
            log.info("Item amount updated. userId={}, productId={}, amount={}", userId, productId, amount);

            return getActiveCart(userId);
        }

    // ─── Admin ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CartSummaryResponse> searchForAdmin(
            UUID userId,
            CartStatus status,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable) {

        Specification<Cart> spec = Specification
                .where(isNotDeleted())
                .and(hasUserId(userId))
                .and(hasStatus(status))
                .and(createdAtFrom(createdFrom))
                .and(createdAtTo(createdTo));

        return PageResponse.of(
                cartRepository.findAll(spec, pageable).map(cartMapper::toSummaryResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartByIdForAdmin(UUID cartId) {
        Cart cart = cartRepository.findById(cartId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_NOT_FOUND));

        if (cart.getItems().isEmpty()) {
            return cartMapper.toEmptyCartResponse(cart, cart.getUserId());
        }

        List<UUID> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .toList();

        Map<UUID, ProductResponse> productMap =
                productServiceHelper.fetchProductMap(productIds);

        return cartMapper.toCartResponse(cart, productMap);
    }
}
