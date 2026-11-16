package com.n11bootcamp.product_service.entity;

import com.n11bootcamp.common.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "products",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_sku", columnNames = "sku")
    },
    indexes = {
        @Index(name = "idx_product_category_id", columnList = "category_id"),
        @Index(name = "idx_product_is_active",   columnList = "is_active"),
        @Index(name = "idx_product_price",        columnList = "price")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "category_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_product_category")
    )
    private Category category;

    @Column(nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private Integer reservedQuantity = 0;

    // ─── Business Methods ────────────────────────────────────────────────────

    public int getAvailableQuantity() {
        return this.quantity - this.reservedQuantity;
    }

    public void decreaseStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Stok düşme miktarı pozitif olmalıdır");
        }
        if (this.quantity < amount) {
            throw new IllegalStateException(
                String.format("Yetersiz stok. Ürün: %s | Mevcut: %d | İstenen: %d",
                    this.getId(), this.quantity, amount)
            );
        }
        this.quantity -= amount;
    }

    public void increaseStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Stok artırma miktarı pozitif olmalıdır");
        }
        this.quantity += amount;
    }

    public void reserveStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Stok rezerve miktarı pozitif olmalıdır");
        }
        if (getAvailableQuantity() < amount) {
            throw new IllegalStateException(
                String.format("Yetersiz stok. Ürün: %s | Müsait: %d | İstenen: %d",
                    this.getId(), getAvailableQuantity(), amount)
            );
        }
        this.reservedQuantity += amount;
    }

    public void releaseReservedStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Miktar pozitif olmalıdır");
        }
        this.reservedQuantity = Math.max(0, this.reservedQuantity - amount);
    }

    public void confirmStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Miktar pozitif olmalıdır");
        }
        this.quantity -= amount;
        this.reservedQuantity = Math.max(0, this.reservedQuantity - amount);
    }

    public boolean hasStock(int requestedAmount) {
        return this.active && !this.isDeleted() && getAvailableQuantity() >= requestedAmount;
    }
}