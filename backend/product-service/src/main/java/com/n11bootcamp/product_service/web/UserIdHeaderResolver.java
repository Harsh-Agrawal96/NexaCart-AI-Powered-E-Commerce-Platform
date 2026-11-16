package com.n11bootcamp.product_service.web;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


@Component
public class UserIdHeaderResolver {

    public UUID resolve(String rawHeaderValue) {
        if (!StringUtils.hasText(rawHeaderValue)) {
            return null;
        }
        try {
            return UUID.fromString(rawHeaderValue.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
