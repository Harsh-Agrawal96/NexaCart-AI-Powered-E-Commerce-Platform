package com.n11bootcamp.product_service.service;

import com.n11bootcamp.common.exception.BusinessException;
import com.n11bootcamp.product_service.dto.response.StockResponse;
import com.n11bootcamp.product_service.entity.Category;
import com.n11bootcamp.product_service.entity.Product;
import com.n11bootcamp.product_service.exception.ProductErrorCode;
import com.n11bootcamp.product_service.mapper.ProductMapper;
import com.n11bootcamp.product_service.publisher.UserBehaviorEventPublisher;
import com.n11bootcamp.product_service.repository.CategoryRepository;
import com.n11bootcamp.product_service.repository.ProductRepository;
import com.n11bootcamp.product_service.service.impl.ProductServiceImpl;
import com.n11bootcamp.product_service.validator.ProductValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    ProductMapper productMapper;

    @Mock
    ProductValidator productValidator;

    @Mock
    UserBehaviorEventPublisher userBehaviorEventPublisher;

    @InjectMocks
    ProductServiceImpl productService;

    @Test
    @DisplayName("Yeterli stok varken rezervasyon: reservedQuantity artar, quantity değişmez")
    void reserveStock_whenSufficientStock_shouldIncreaseReservedQuantityAndKeepQuantitySame() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Product product = buildProduct(productId, 10, 2);

        given(productRepository.findByIdForUpdate(productId)).willReturn(Optional.of(product));
        // validator success → exception yok

        // Act
        StockResponse response = productService.reserveStock(productId, 5);

        // Assert
        assertThat(product.getQuantity()).isEqualTo(10);
        assertThat(product.getReservedQuantity()).isEqualTo(7);
        assertThat(product.getAvailableQuantity()).isEqualTo(3);

        assertThat(response.productId()).isEqualTo(productId);
        assertThat(response.totalQuantity()).isEqualTo(10);
        assertThat(response.reservedQuantity()).isEqualTo(7);
        assertThat(response.availableQuantity()).isEqualTo(3);
        assertThat(response.success()).isTrue();
    }

    @Test
    @DisplayName("Yetersiz stok varken rezervasyon: exception fırlatır")
    void reserveStock_whenInsufficientStock_shouldThrowBusinessException() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Product product = buildProduct(productId, 3, 2); // available=1

        given(productRepository.findByIdForUpdate(productId)).willReturn(Optional.of(product));
        willThrow(new BusinessException(ProductErrorCode.INSUFFICIENT_STOCK))
                .given(productValidator).validateStock(product, 5);

        // Act + Assert
        assertThatThrownBy(() -> productService.reserveStock(productId, 5))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.INSUFFICIENT_STOCK)
                );

        assertThat(product.getQuantity()).isEqualTo(3);
        assertThat(product.getReservedQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rezervasyon sırasında ürün bulunamazsa: exception fırlatır")
    void reserveStock_whenProductNotFound_shouldThrowBusinessException() {
        // Arrange
        UUID productId = UUID.randomUUID();
        given(productRepository.findByIdForUpdate(productId)).willReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> productService.reserveStock(productId, 5))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND)
                );
    }

    @Test
    @DisplayName("Ödeme başarılıysa confirmStock: quantity düşer, reservedQuantity azalır")
    void confirmStock_whenCalled_shouldDecreaseQuantityAndReservedQuantity() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Product product = buildProduct(productId, 10, 5);

        given(productRepository.findByIdForUpdate(productId)).willReturn(Optional.of(product));

        // Act
        StockResponse response = productService.confirmStock(productId, 3);

        // Assert
        assertThat(product.getQuantity()).isEqualTo(7);
        assertThat(product.getReservedQuantity()).isEqualTo(2);
        assertThat(product.getAvailableQuantity()).isEqualTo(5);

        assertThat(response.totalQuantity()).isEqualTo(7);
        assertThat(response.reservedQuantity()).isEqualTo(2);
        assertThat(response.availableQuantity()).isEqualTo(5);
        assertThat(response.success()).isTrue();
    }

    @Test
    @DisplayName("Stok onayı sırasında ürün bulunamazsa: exception fırlatır")
    void confirmStock_whenProductNotFound_shouldThrowBusinessException() {
        // Arrange
        UUID productId = UUID.randomUUID();
        given(productRepository.findByIdForUpdate(productId)).willReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> productService.confirmStock(productId, 3))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND)
                );
    }

    @Test
    @DisplayName("Rezervasyon iade edilince: reservedQuantity eski değere döner")
    void releaseReservedStock_whenCalled_shouldDecreaseReservedQuantity() {
        // Arrange
        UUID productId = UUID.randomUUID();
        Product product = buildProduct(productId, 10, 6);

        given(productRepository.findByIdForUpdate(productId)).willReturn(Optional.of(product));

        // Act
        StockResponse response = productService.releaseReservedStock(productId, 4);

        // Assert
        assertThat(product.getQuantity()).isEqualTo(10);
        assertThat(product.getReservedQuantity()).isEqualTo(2);
        assertThat(product.getAvailableQuantity()).isEqualTo(8);

        assertThat(response.totalQuantity()).isEqualTo(10);
        assertThat(response.reservedQuantity()).isEqualTo(2);
        assertThat(response.availableQuantity()).isEqualTo(8);
        assertThat(response.success()).isTrue();
    }

    @Test
    @DisplayName("Rezervasyon iadesinde ürün bulunamazsa: exception fırlatır")
    void releaseReservedStock_whenProductNotFound_shouldThrowBusinessException() {
        // Arrange
        UUID productId = UUID.randomUUID();
        given(productRepository.findByIdForUpdate(productId)).willReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> productService.releaseReservedStock(productId, 1))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND)
                );
    }

    private static Product buildProduct(UUID id, int quantity, int reservedQuantity) {
        Category category = Category.builder()
                .name("Elektronik")
                .slug("elektronik")
                .active(true)
                .build();

        Product p = Product.builder()
                .name("Test Ürün")
                .description("desc")
                .sku("SKU-" + id)
                .price(BigDecimal.valueOf(99.90))
                .quantity(quantity)
                .reservedQuantity(reservedQuantity)
                .active(true)
                .category(category)
                .build();
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }
}

