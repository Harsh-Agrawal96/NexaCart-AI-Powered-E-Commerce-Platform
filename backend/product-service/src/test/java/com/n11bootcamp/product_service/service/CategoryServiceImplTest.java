package com.n11bootcamp.product_service.service;

import com.n11bootcamp.common.exception.BusinessException;
import com.n11bootcamp.product_service.dto.request.CreateCategoryRequest;
import com.n11bootcamp.product_service.dto.request.UpdateCategoryRequest;
import com.n11bootcamp.product_service.dto.response.CategoryResponse;
import com.n11bootcamp.product_service.entity.Category;
import com.n11bootcamp.product_service.exception.CategoryErrorCode;
import com.n11bootcamp.product_service.mapper.CategoryMapper;
import com.n11bootcamp.product_service.repository.CategoryRepository;
import com.n11bootcamp.product_service.service.impl.CategoryServiceImpl;
import com.n11bootcamp.product_service.validator.CategoryValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    CategoryMapper categoryMapper;

    @Mock
    CategoryValidator categoryValidator;

    @InjectMocks
    CategoryServiceImpl categoryService;

    @Test
    @DisplayName("Geçerli isimle kategori oluşturulunca: response dönmeli, name doğru olmalı")
    void create_whenValidRequest_shouldReturnResponseWithCorrectName() {
        // Arrange
        CreateCategoryRequest request = new CreateCategoryRequest(
                "Elektronik",
                "elektronik",
                "Açıklama",
                null,
                1
        );

        UUID id = UUID.randomUUID();
        Category saved = Category.builder()
                .name(request.name())
                .slug(request.slug())
                .description(request.description())
                .imageUrl(request.imageUrl())
                .displayOrder(request.displayOrder())
                .active(true)
                .build();
        ReflectionTestUtils.setField(saved, "id", id);

        CategoryResponse expected = new CategoryResponse(
                id,
                request.name(),
                request.slug(),
                request.description(),
                request.imageUrl(),
                request.displayOrder(),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        willDoNothing().given(categoryValidator).validateCreate(request);
        given(categoryRepository.saveAndFlush(any(Category.class))).willReturn(saved);
        given(categoryMapper.toEntity(request)).willReturn(saved);
        given(categoryMapper.toResponse(saved)).willReturn(expected);

        // Act
        CategoryResponse response = categoryService.create(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Elektronik");
        assertThat(response.slug()).isEqualTo("elektronik");
        assertThat(response.active()).isTrue();
    }

    @Test
    @DisplayName("Duplicate isimle oluşturulunca: exception fırlatır")
    void create_whenDuplicateName_shouldThrowBusinessException() {
        // Arrange
        CreateCategoryRequest request = new CreateCategoryRequest(
                "Elektronik",
                "elektronik",
                null,
                null,
                0
        );

        willThrow(new BusinessException(CategoryErrorCode.CATEGORY_NAME_ALREADY_EXISTS))
                .given(categoryValidator).validateCreate(request);

        // Act + Assert
        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(CategoryErrorCode.CATEGORY_NAME_ALREADY_EXISTS)
                );
    }

    @Test
    @DisplayName("Olmayan kategori güncellenince: not found exception fırlatır")
    void update_whenCategoryNotFound_shouldThrowBusinessException() {
        // Arrange
        UUID categoryId = UUID.randomUUID();
        UpdateCategoryRequest request = new UpdateCategoryRequest(
                "Yeni İsim",
                null,
                null,
                null,
                null,
                null
        );

        given(categoryRepository.findByIdAndDeletedFalse(categoryId)).willReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> categoryService.update(categoryId, request))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND)
                );
    }
}

