package com.n11bootcamp.ai_service.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Logger;
import feign.Response;
import feign.codec.Decoder;

import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;

@Configuration
public class FeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Decoder feignDecoder(ObjectMapper objectMapper) {
        Decoder jacksonDecoder = new Decoder() {
            @Override
            public Object decode(Response response, Type type) throws IOException {
                if (response.body() == null) {
                    return null;
                }
                try (InputStream is = response.body().asInputStream()) {
                    JavaType javaType = objectMapper.getTypeFactory().constructType(type);
                    return objectMapper.readValue(is, javaType);
                }
            }
        };
        return new ResponseEntityDecoder(jacksonDecoder);
    }
}

