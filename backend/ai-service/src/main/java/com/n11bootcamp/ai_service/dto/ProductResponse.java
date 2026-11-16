package com.n11bootcamp.ai_service.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductResponse {

    private UUID id;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private Integer quantity;
    @JsonProperty("categoryName")
    private String categoryNameValue;

    @JsonProperty("category")
    private CategoryJson category;

    private String imageUrl;
    private Boolean active;
    @JsonIgnore
    public String getCategoryName() {
        if (categoryNameValue != null && !categoryNameValue.isBlank()) {
            return categoryNameValue;
        }
        return category != null && category.getName() != null ? category.getName() : null;
    }

    @JsonIgnore
    public void setCategoryName(String name) {
        this.categoryNameValue = name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryJson {
        private String name;
    }
}
