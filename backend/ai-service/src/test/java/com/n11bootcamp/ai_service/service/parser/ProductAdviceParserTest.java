package com.n11bootcamp.ai_service.service.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bootcamp.ai_service.dto.response.ProductAdviceResponse;

class ProductAdviceParserTest {

    private ProductAdviceParser parser;

    @BeforeEach
    void setUp() {
        parser = new ProductAdviceParser(new ObjectMapper());
    }

    @Test
    @DisplayName("Geçerli JSON çıkarılıp alanlar doldurulur")
    void parse_validJson_extractsFields() {
        String raw = "Önce metin {\"verdict\":\"RECOMMENDED\",\"summary\":\"İyi ürün\",\"pros\":[\"hızlı\"],\"cons\":[\"pahalı\"],\"confidence\":0.85} sonra";

        ProductAdviceResponse out = parser.parse(raw);

        assertThat(out.getVerdict()).isEqualTo("RECOMMENDED");
        assertThat(out.getSummary()).isEqualTo("İyi ürün");
        assertThat(out.getPros()).containsExactly("hızlı");
        assertThat(out.getCons()).containsExactly("pahalı");
        assertThat(out.getConfidence()).isEqualTo(0.85);
    }

    @Test
    @DisplayName("JSON yoksa MIXED ve varsayılan özet")
    void parse_noJson_returnsMixedAndFallbackSummary() {
        ProductAdviceResponse out = parser.parse("sadece düz metin");

        assertThat(out.getVerdict()).isEqualTo("MIXED");
        assertThat(out.getSummary()).isEqualTo("sadece düz metin");
        assertThat(out.getPros()).isEmpty();
        assertThat(out.getCons()).isEmpty();
        assertThat(out.getConfidence()).isEqualTo(0.5);
    }

    @Test
    @DisplayName("null/boş giriş — MIXED ve Türkçe varsayılan özet")
    void parse_blank_returnsDefaultSummary() {
        ProductAdviceResponse out = parser.parse(null);

        assertThat(out.getVerdict()).isEqualTo("MIXED");
        assertThat(out.getSummary()).isEqualTo("Yanıt alınamadı.");
        assertThat(out.getConfidence()).isEqualTo(0.5);
    }

    @Test
    @DisplayName("Sözdizimi bozuk JSON objesi — MIXED, ham metin; düşük güven")
    void parse_invalidJson_returnsMixedWithRawSummary() {
        // Hem '{' hem '}' olduğu için extractJson substring döner; readTree parse edemez → catch dalı (0.3)
        String raw = "{\"verdict\":}";

        ProductAdviceResponse out = parser.parse(raw);

        assertThat(out.getVerdict()).isEqualTo("MIXED");
        assertThat(out.getSummary()).isEqualTo(raw);
        assertThat(out.getConfidence()).isEqualTo(0.3);
    }
}
