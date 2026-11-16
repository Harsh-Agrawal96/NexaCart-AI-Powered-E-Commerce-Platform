package com.n11bootcamp.ai_service.service.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.n11bootcamp.ai_service.dto.ProductResponse;
import com.n11bootcamp.ai_service.dto.ReviewResponse;

class ProductAdvicePromptBuilderTest {

    private ProductAdvicePromptBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ProductAdvicePromptBuilder();
    }

    @Test
    @DisplayName("Ürün ve yorumlar prompt içinde yer alır")
    void build_includesProductAndReviews() {
        UUID pid = UUID.randomUUID();
        ProductResponse p = new ProductResponse();
        p.setId(pid);
        p.setName("Kulaklık");
        p.setPrice(BigDecimal.valueOf(99.99));
        p.setCategoryName("Elektronik");
        p.setQuantity(10);
        p.setActive(true);
        p.setDescription("Uzun açıklama");

        ReviewResponse r = new ReviewResponse();
        r.setRating(5);
        r.setUserFirstName("Ali");
        r.setUserLastName("Yılmaz");
        r.setComment("Çok memnunum");

        String prompt = builder.build(p, List.of(r), "Alınır mı?");

        assertThat(prompt).contains("Alınır mı?");
        assertThat(prompt).contains("Kulaklık");
        assertThat(prompt).contains("99.99");
        assertThat(prompt).contains("Elektronik");
        assertThat(prompt).contains("Ali");
        assertThat(prompt).contains("Yılmaz");
        assertThat(prompt).contains("Çok memnunum");
        assertThat(prompt).contains("JSON");
    }

    @Test
    @DisplayName("Ürün null ise placeholder")
    void build_nullProduct_showsMissing() {
        String prompt = builder.build(null, List.of(), null);

        assertThat(prompt).contains("(ürün bulunamadı)");
        assertThat(prompt).contains("(yorum yok)");
    }

    @Test
    @DisplayName("Boş soru satırı eklenmez")
    void build_blankQuestion_skipsQuestionLine() {
        String prompt = builder.build(new ProductResponse(), List.of(), "   ");

        assertThat(prompt).doesNotContain("Kullanıcı sorusu:");
    }
}
