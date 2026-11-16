package com.n11bootcamp.ai_service.service.prompt;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.n11bootcamp.ai_service.dto.ProductResponse;
import com.n11bootcamp.ai_service.dto.UserBehaviorRecentItemDto;

@Component
public class BehaviorRecommendationPromptBuilder {

    public String build(List<UserBehaviorRecentItemDto> behaviors,
                        List<ProductResponse> candidates,
                        UUID currentProductId) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sen bir e-ticaret asistanısın. Kullanıcının son ürün davranışlarına göre hangi ürünleri önerebileceğini seç.\n");
        sb.append("Aşağıdaki aday ürün listesi alışveriş niyetine göre sıralıdır: sepete ekleme ve ödeme başlatma, ");
        sb.append("yalnız ürün görüntülemeye göre önce gelir.\n");
        sb.append("Yanıtı SADECE JSON olarak döndür.\n");
        sb.append("JSON şeması: {\"summary\":\"kısa Türkçe açıklama\",\"recommendedProductIds\":[\"uuid\",...],\"confidence\":0.0}\n");
        sb.append("recommendedProductIds sırası önem sırasıdır (en uygun önce). ");
        sb.append("Sadece aşağıda listelenen ürün id'lerinden seç; uydurma id yazma.\n\n");

        if (currentProductId != null) {
            sb.append("Kullanıcı şu an şu ürün sayfasında: ").append(currentProductId).append("\n\n");
        }

        sb.append("Son davranışlar (yeniden eskiye):\n");
        if (behaviors == null || behaviors.isEmpty()) {
            sb.append("- (yok)\n");
        } else {
            for (UserBehaviorRecentItemDto b : behaviors) {
                sb.append("- productId: ").append(b.getProductId())
                        .append(", type: ").append(b.getBehaviorType())
                        .append(", at: ").append(b.getOccurredAt() != null ? b.getOccurredAt() : "—")
                        .append("\n");
            }
        }

        sb.append("\nÖnerilebilecek ürünler (sadece bunların id'lerini kullan):\n");
        if (candidates == null || candidates.isEmpty()) {
            sb.append("- (ürün listesi boş)\n");
        } else {
            for (ProductResponse p : candidates) {
                sb.append("- id: ").append(p.getId())
                        .append(", name: ").append(nvl(p.getName()))
                        .append(", price: ").append(p.getPrice() != null ? p.getPrice() : "—")
                        .append(", category: ").append(nvl(categoryLabel(p)))
                        .append("\n");
            }
        }

        sb.append("\nKısıt: Veri zayıfsa confidence düşük olsun; abartılı id listesi verme.\n");
        return sb.toString();
    }

    private static String categoryLabel(ProductResponse p) {
        if (p == null) {
            return "";
        }
        String c = p.getCategoryName();
        return c != null ? c : "";
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
