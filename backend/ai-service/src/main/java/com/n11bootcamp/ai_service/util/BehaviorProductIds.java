package com.n11bootcamp.ai_service.util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.n11bootcamp.ai_service.dto.UserBehaviorRecentItemDto;
import com.n11bootcamp.common.enums.UserBehaviorType;

/**
 * Davranış kayıtlarından ürün kimliklerini çıkarır; yüksek alışveriş niyeti (sepet vb.)
 * düşük etkileşimden (sadece görüntüleme) önce sıralanır.
 */
public final class BehaviorProductIds {

    private BehaviorProductIds() {}

    /**
     * Her ürün için en güçlü davranış sinyalini ve eşitlikte en yeni zamanı kullanarak sıralar.
     */
    public static List<UUID> rankedUniqueByIntent(List<UserBehaviorRecentItemDto> items, int maxUnique) {
        if (items == null || items.isEmpty() || maxUnique <= 0) {
            return List.of();
        }

        Map<UUID, Agg> perProduct = new HashMap<>();
        for (UserBehaviorRecentItemDto row : items) {
            if (row == null || row.getProductId() == null) {
                continue;
            }
            UserBehaviorType type = row.getBehaviorType();
            if (type == null) {
                continue;
            }
            int w = intentWeight(type);
            Instant at = row.getOccurredAt();
            UUID pid = row.getProductId();
            perProduct.compute(pid, (k, old) -> {
                if (old == null) {
                    return new Agg(w, at);
                }
                old.absorbRow(w, at);
                return old;
            });
        }

        List<UUID> ranked =
                perProduct.entrySet().stream()
                        .sorted(
                                Comparator.<Map.Entry<UUID, Agg>, Integer>comparing(e -> e.getValue().bestWeight)
                                        .reversed()
                                        .thenComparing(
                                                e -> e.getValue().latestAtForBest,
                                                Comparator.nullsLast(Comparator.reverseOrder())))
                        .map(Map.Entry::getKey)
                        .limit(maxUnique)
                        .toList();

        return new ArrayList<>(ranked);
    }

    /**
     * Sepet / ödeme niyeti görüntülemeden yüksek; çıkarma düşük sinyal.
     */
    static int intentWeight(UserBehaviorType type) {
        return switch (type) {
            case ADD_TO_CART -> 100;
            case CHECKOUT_STARTED -> 95;
            case PRODUCT_VIEWED -> 10;
            case REMOVE_FROM_CART -> 3;
        };
    }

    private static final class Agg {
        int bestWeight;
        Instant latestAtForBest;

        Agg(int bestWeight, Instant latestAtForBest) {
            this.bestWeight = bestWeight;
            this.latestAtForBest = latestAtForBest;
        }

        void absorbRow(int w, Instant at) {
            if (w > bestWeight) {
                bestWeight = w;
                latestAtForBest = at;
            } else if (w == bestWeight
                    && at != null
                    && (latestAtForBest == null || at.isAfter(latestAtForBest))) {
                latestAtForBest = at;
            }
        }
    }
}
