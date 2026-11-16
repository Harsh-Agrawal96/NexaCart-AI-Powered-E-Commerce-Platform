package com.n11bootcamp.ai_service.service;

import com.n11bootcamp.ai_service.dto.request.ProductAdviceRequest;
import com.n11bootcamp.ai_service.dto.response.ProductAdviceResponse;

public interface ProductAdviceService {

    ProductAdviceResponse getAdvice(ProductAdviceRequest req);
}
