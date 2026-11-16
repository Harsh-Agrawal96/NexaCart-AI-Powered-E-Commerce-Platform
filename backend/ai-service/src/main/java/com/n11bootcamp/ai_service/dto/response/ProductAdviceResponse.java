package com.n11bootcamp.ai_service.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class ProductAdviceResponse {

    private String verdict;
    private String summary;
    private List<String> pros;
    private List<String> cons;
    private Double confidence;
    private Integer usedReviewCount;
}
