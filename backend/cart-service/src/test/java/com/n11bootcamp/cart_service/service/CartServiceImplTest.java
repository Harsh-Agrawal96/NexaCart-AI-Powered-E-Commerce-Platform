package com.n11bootcamp.cart_service.service;

import com.n11bootcamp.cart_service.client.ProductServiceHelper;
import com.n11bootcamp.cart_service.client.dto.ProductResponse;
import com.n11bootcamp.cart_service.dto.request.AddItemRequest;
import com.n11bootcamp.cart_service.dto.response.CartResponse;
import com.n11bootcamp.cart_service.entity.Cart;
import com.n11bootcamp.cart_service.entity.CartItem;
import com.n11bootcamp.cart_service.entity.enums.CartStatus;
import com.n11bootcamp.cart_service.exception.CartErrorCode;
import com.n11bootcamp.cart_service.mapper.CartMapper;
import com.n11bootcamp.cart_service.publisher.UserBehaviorEventPublisher;
import com.n11bootcamp.cart_service.repository.CartItemRepository;
import com.n11bootcamp.cart_service.repository.CartRepository;
import com.n11bootcamp.cart_service.service.impl.CartServiceImpl;
import com.n11bootcamp.cart_service.validator.CartValidator;
import com.n11bootcamp.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    CartRepository cartRepository;

    @Mock
    CartItemRepository cartItemRepository;

    @Mock
    ProductServiceHelper productServiceHelper;

    @Mock
    CartValidator cartValidator;

    @Mock
    CartMapper cartMapper;

    @Mock
    UserBehaviorEventPublisher userBehaviorEventPublisher;

    @InjectMocks
    CartServiceImpl cartService;

    @Test
    @DisplayName("Aynı ürün iki kez eklenince: tek item, quantity toplamı doğru")
    void addItem_whenSameProductAddedTwice_shouldKeepSingleItemAndSumAmount() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Cart cart = new Cart();
        ReflectionTestUtils.setField(cart, "id", cartId);
        cart.setUserId(userId);
        cart.setStatus(CartStatus.ACTIVE);

        ProductResponse product = buildActiveProduct(productId, 100);
        Map<UUID, ProductResponse> productMap = Map.of(productId, product);

        given(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).willReturn(Optional.of(cart));
        given(productServiceHelper.fetchProductMap(anyList())).willReturn(productMap);
        given(cartValidator.requireProductInBatch(productMap, productId)).willReturn(product);
        given(cartRepository.save(cart)).willReturn(cart);

        // addItem sonunda getActiveCart çağrılır → mapper stub
        CartResponse dummyResponse = new CartResponse(cartId, userId, List.of(), BigDecimal.ZERO);
        given(cartMapper.toCartResponse(eq(cart), eq(productMap))).willReturn(dummyResponse);

        // 1. çağrı: item yok → create
        given(cartItemRepository.findByCartIdAndProductId(cartId, productId)).willReturn(Optional.empty());

        // Act 1
        cartService.addItem(userId, new AddItemRequest(productId, 2));

        // Assert 1
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().getFirst().getProductId()).isEqualTo(productId);
        assertThat(cart.getItems().getFirst().getAmount()).isEqualTo(2);

        // 2. çağrı: aynı item bulunur
        given(cartItemRepository.findByCartIdAndProductId(cartId, productId))
                .willReturn(Optional.of(cart.getItems().getFirst()));

        // Act 2
        cartService.addItem(userId, new AddItemRequest(productId, 3));

        // Assert 2
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().getFirst().getAmount()).isEqualTo(5);

        verify(userBehaviorEventPublisher, times(2)).publishAddToCart(eq(userId), eq(productId));
    }

    @Test
    @DisplayName("Farklı ürün eklenince: yeni item oluşur")
    void addItem_whenDifferentProductsAdded_shouldCreateNewItem() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID product1 = UUID.randomUUID();
        UUID product2 = UUID.randomUUID();

        Cart cart = new Cart();
        ReflectionTestUtils.setField(cart, "id", cartId);
        cart.setUserId(userId);
        cart.setStatus(CartStatus.ACTIVE);

        ProductResponse p1 = buildActiveProduct(product1, 100);
        ProductResponse p2 = buildActiveProduct(product2, 100);

        given(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).willReturn(Optional.of(cart));
        given(cartRepository.save(cart)).willReturn(cart);
        given(cartItemRepository.findByCartIdAndProductId(cartId, product1)).willReturn(Optional.empty());
        given(cartItemRepository.findByCartIdAndProductId(cartId, product2)).willReturn(Optional.empty());

        given(cartValidator.requireProductInBatch(anyMap(), eq(product1))).willReturn(p1);
        given(cartValidator.requireProductInBatch(anyMap(), eq(product2))).willReturn(p2);

        // getActiveCart çağrılarında mapper stub: her seferinde mevcut item sayısını baz almayacağız, dummy yeterli
        given(productServiceHelper.fetchProductMap(anyList()))
                .willReturn(Map.of(product1, p1, product2, p2));
        given(cartMapper.toCartResponse(eq(cart), anyMap()))
                .willReturn(new CartResponse(cartId, userId, List.of(), BigDecimal.ZERO));

        // Act
        cartService.addItem(userId, new AddItemRequest(product1, 1));
        cartService.addItem(userId, new AddItemRequest(product2, 1));

        // Assert
        assertThat(cart.getItems()).hasSize(2);

        verify(userBehaviorEventPublisher).publishAddToCart(eq(userId), eq(product1));
        verify(userBehaviorEventPublisher).publishAddToCart(eq(userId), eq(product2));
    }

    @Test
    @DisplayName("amount=0 güncellenince: item silinir")
    void updateItemAmount_whenAmountIsZero_shouldRemoveItem() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Cart cart = new Cart();
        ReflectionTestUtils.setField(cart, "id", cartId);
        cart.setUserId(userId);
        cart.setStatus(CartStatus.ACTIVE);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProductId(productId);
        item.setAmount(2);
        cart.getItems().add(item);

        ProductResponse product = buildActiveProduct(productId, 100);
        Map<UUID, ProductResponse> productMap = Map.of(productId, product);

        given(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).willReturn(Optional.of(cart));
        given(cartValidator.requireActiveCart(Optional.of(cart), userId)).willReturn(cart);

        given(cartItemRepository.findByCartIdAndProductId(cartId, productId)).willReturn(Optional.of(item));
        given(cartValidator.requireCartItem(Optional.of(item), productId)).willReturn(item);

        given(productServiceHelper.fetchProductMap(eq(List.of(productId)))).willReturn(productMap);
        given(cartValidator.requireProductInBatch(productMap, productId)).willReturn(product);

        given(cartRepository.save(cart)).willReturn(cart);
        given(cartMapper.toEmptyCartResponse(cart, userId))
                .willReturn(new CartResponse(cartId, userId, List.of(), BigDecimal.ZERO));

        // Act
        CartResponse response = cartService.updateItemAmount(userId, productId, 0);

        // Assert
        assertThat(cart.getItems()).isEmpty();
        assertThat(response.items()).isEmpty();
        assertThat(response.grandTotal()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(userBehaviorEventPublisher).publishRemoveFromCart(eq(userId), eq(productId));
    }

    @Test
    @DisplayName("Ürün satırdan çıkarılınca: REMOVE_FROM_CART yayınlanır")
    void removeItem_whenItemExists_shouldPublishRemoveFromCart() {
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Cart cart = new Cart();
        ReflectionTestUtils.setField(cart, "id", cartId);
        cart.setUserId(userId);
        cart.setStatus(CartStatus.ACTIVE);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProductId(productId);
        item.setAmount(1);
        cart.getItems().add(item);

        given(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).willReturn(Optional.of(cart));
        given(cartValidator.requireActiveCart(Optional.of(cart), userId)).willReturn(cart);
        given(cartItemRepository.findByCartIdAndProductId(cartId, productId)).willReturn(Optional.of(item));
        given(cartValidator.requireCartItem(Optional.of(item), productId)).willReturn(item);
        given(cartRepository.save(cart)).willReturn(cart);

        cartService.removeItem(userId, productId);

        assertThat(cart.getItems()).isEmpty();
        verify(userBehaviorEventPublisher).publishRemoveFromCart(eq(userId), eq(productId));
    }

    @Test
    @DisplayName("Olmayan ürün çıkarılınca: ITEM_NOT_FOUND exception fırlatır")
    void removeItem_whenCartItemNotFound_shouldThrowBusinessException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Cart cart = new Cart();
        ReflectionTestUtils.setField(cart, "id", cartId);
        cart.setUserId(userId);
        cart.setStatus(CartStatus.ACTIVE);

        given(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).willReturn(Optional.of(cart));
        given(cartValidator.requireActiveCart(Optional.of(cart), userId)).willReturn(cart);

        given(cartItemRepository.findByCartIdAndProductId(cartId, productId)).willReturn(Optional.empty());
        willThrow(new BusinessException(CartErrorCode.ITEM_NOT_FOUND))
                .given(cartValidator).requireCartItem(Optional.empty(), productId);

        // Act + Assert
        BusinessException thrown =
                assertThrows(BusinessException.class, () -> cartService.removeItem(userId, productId));
        assertThat(thrown.getErrorCode()).isEqualTo(CartErrorCode.ITEM_NOT_FOUND);

        verify(userBehaviorEventPublisher, never()).publishRemoveFromCart(any(), any());
    }

    @Test
    @DisplayName("Ödeme başarılıysa: aktif sepet ORDERED olur")
    void clearCart_whenActiveCartExists_shouldSetStatusOrdered() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();

        Cart cart = new Cart();
        ReflectionTestUtils.setField(cart, "id", cartId);
        cart.setUserId(userId);
        cart.setStatus(CartStatus.ACTIVE);

        given(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).willReturn(Optional.of(cart));
        given(cartRepository.save(cart)).willReturn(cart);

        // Act
        cartService.clearCart(userId);

        // Assert
        assertThat(cart.getStatus()).isEqualTo(CartStatus.ORDERED);
    }

    private static ProductResponse buildActiveProduct(UUID id, int availableQuantity) {
        return new ProductResponse(
                id,
                "Test Ürün",
                "SKU-" + id,
                BigDecimal.valueOf(99.90),
                true,
                availableQuantity
        );
    }
}

