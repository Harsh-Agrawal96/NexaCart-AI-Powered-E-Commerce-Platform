package com.n11bootcamp.ai_service.cache;

import java.lang.reflect.Method;
import java.util.Locale;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import com.n11bootcamp.ai_service.dto.request.ProductAdviceRequest;

@Component("productAdviceCacheKeyGenerator")
public class ProductAdviceCacheKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        if (params.length == 0 || !(params[0] instanceof ProductAdviceRequest req)) {
            throw new IllegalArgumentException("Cache key için ProductAdviceRequest bekleniyor");
        }
        String q = req.getQuestion() == null ? "" : req.getQuestion().trim();
        q = q.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        return String.valueOf(req.getProductId()) + "|" + q;
    }
}
