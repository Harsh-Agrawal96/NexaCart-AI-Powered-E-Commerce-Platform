package com.n11bootcamp.ai_service.service.prompt;

import java.util.List;

import org.springframework.stereotype.Component;

import com.n11bootcamp.ai_service.dto.ProductResponse;
import com.n11bootcamp.ai_service.dto.ReviewResponse;

@Component
public class ProductAdvicePromptBuilder {

    public String build(ProductResponse product, List<ReviewResponse> reviews, String question) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sen bir e-ticaret asistanısın. Kullanıcı bir ürün için 'alınır mı' tavsiyesi istiyor.\n");
        sb.append("Cevabı SADECE JSON olarak döndür.\n");
        sb.append("JSON şeması: {\"verdict\":\"RECOMMENDED|MIXED|NOT_RECOMMENDED\",\"summary\":\"...\",\"pros\":[\"...\"],\"cons\":[\"...\"],\"confidence\":0.0}\n\n");

        if (question != null && !question.isBlank()) {
            sb.append("Kullanıcı sorusu: ").append(question).append("\n\n");
        }

        sb.append("Ürün:\n");
        if (product != null) {
            sb.append("- name: ").append(nvl(product.getName())).append("\n");
            sb.append("- price: ").append(product.getPrice() != null ? product.getPrice() : "—").append("\n");
            sb.append("- category: ").append(nvl(product.getCategoryName())).append("\n");
            sb.append("- quantity: ").append(product.getQuantity() != null ? product.getQuantity() : "—").append("\n");
            sb.append("- active: ").append(product.getActive() != null ? product.getActive() : "—").append("\n");
            sb.append("- description: ").append(trimMax(product.getDescription(), 600)).append("\n");
        } else {
            sb.append("- (ürün bulunamadı)\n");
        }

        sb.append("\nYorumlar (en yeni):\n");
        if (reviews == null || reviews.isEmpty()) {
            sb.append("- (yorum yok)\n");
        } else {
            for (ReviewResponse r : reviews) {
                sb.append("- rating: ").append(r.getRating() != null ? r.getRating() : "—");
                String name = (nvl(r.getUserFirstName()) + " " + nvl(r.getUserLastName())).trim();
                if (!name.isBlank()) sb.append(", user: ").append(name);
                sb.append(", comment: ").append(trimMax(r.getComment(), 400)).append("\n");
            }
        }

        sb.append("\nKısıt: Uydurma yapma. Yorum yoksa bunu belirt. Güven düşükse confidence düşük olsun.\n");
        return sb.toString();
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String trimMax(String s, int max) {
        if (s == null) return "";
        String t = s.trim();
        if (t.length() <= max) return t;
        return t.substring(0, max) + "...";
    }
}

