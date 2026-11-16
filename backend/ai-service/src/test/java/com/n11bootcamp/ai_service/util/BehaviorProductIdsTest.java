package com.n11bootcamp.ai_service.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.n11bootcamp.ai_service.dto.UserBehaviorRecentItemDto;
import com.n11bootcamp.common.enums.UserBehaviorType;

class BehaviorProductIdsTest {

    @Test
    @DisplayName("Sepete eklenen ürün yalnız görüntülenenden önce sıralanır")
    void ranked_cartBeforeViewOnly() {
        UUID viewedOnly = UUID.randomUUID();
        UUID inCart = UUID.randomUUID();

        var rowView = row(viewedOnly, UserBehaviorType.PRODUCT_VIEWED, "2026-05-02T12:00:00Z");
        var rowCart = row(inCart, UserBehaviorType.ADD_TO_CART, "2026-05-02T11:00:00Z");

        List<UserBehaviorRecentItemDto> items = List.of(rowView, rowCart);

        List<UUID> out = BehaviorProductIds.rankedUniqueByIntent(items, 10);

        assertThat(out.getFirst()).isEqualTo(inCart);
        assertThat(out.get(1)).isEqualTo(viewedOnly);
    }

    @Test
    @DisplayName("Aynı üründe sepet, daha önceki görüntülemeyi bastırır")
    void ranked_sameProduct_cartDominatesView() {
        UUID pid = UUID.randomUUID();
        var newerView = row(pid, UserBehaviorType.PRODUCT_VIEWED, "2026-05-02T13:00:00Z");
        var olderCart = row(pid, UserBehaviorType.ADD_TO_CART, "2026-05-02T10:00:00Z");

        List<UserBehaviorRecentItemDto> items = List.of(newerView, olderCart);

        List<UUID> out = BehaviorProductIds.rankedUniqueByIntent(items, 5);

        assertThat(out).containsExactly(pid);
    }

    private static UserBehaviorRecentItemDto row(UUID productId, UserBehaviorType type, String isoInstant) {
        var dto = new UserBehaviorRecentItemDto();
        dto.setProductId(productId);
        dto.setBehaviorType(type);
        dto.setOccurredAt(Instant.parse(isoInstant));
        return dto;
    }
}
