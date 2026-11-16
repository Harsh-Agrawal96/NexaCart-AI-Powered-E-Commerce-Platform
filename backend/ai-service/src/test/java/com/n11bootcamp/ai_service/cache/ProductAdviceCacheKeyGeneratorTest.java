package com.n11bootcamp.ai_service.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.n11bootcamp.ai_service.dto.request.ProductAdviceRequest;
import com.n11bootcamp.ai_service.service.ProductAdviceService;

class ProductAdviceCacheKeyGeneratorTest {

    private ProductAdviceCacheKeyGenerator generator;
    private Method getAdviceMethod;

    @BeforeEach
    void setUp() throws Exception {
        generator = new ProductAdviceCacheKeyGenerator();
        getAdviceMethod = ProductAdviceService.class.getMethod("getAdvice", ProductAdviceRequest.class);
    }

    @Test
    @DisplayName("productId ve normalize edilmiş soru birleşir")
    void generate_normalizesWhitespaceAndCase() {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        ProductAdviceRequest req = new ProductAdviceRequest();
        req.setProductId(id);
        req.setQuestion("  Merhaba   Dünya ");

        Object key = generator.generate(null, getAdviceMethod, req);

        assertThat(key).isEqualTo(id + "|merhaba dünya");
    }

    @Test
    @DisplayName("null soru boş string olarak anahtara girer")
    void generate_nullQuestion_emptySuffix() {
        UUID id = UUID.randomUUID();
        ProductAdviceRequest req = new ProductAdviceRequest();
        req.setProductId(id);
        req.setQuestion(null);

        Object key = generator.generate(null, getAdviceMethod, req);

        assertThat(key).isEqualTo(id.toString() + "|");
    }

    @Test
    @DisplayName("İlk parametre ProductAdviceRequest değilse hata")
    void generate_wrongParamType_throws() {
        assertThatThrownBy(() -> generator.generate(null, getAdviceMethod, "x"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ProductAdviceRequest");
    }
}
