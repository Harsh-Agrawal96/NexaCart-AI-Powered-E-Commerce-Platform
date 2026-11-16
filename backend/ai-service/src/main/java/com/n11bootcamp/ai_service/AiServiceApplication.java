package com.n11bootcamp.ai_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

import com.n11bootcamp.ai_service.config.GeminiProperties;

@SpringBootApplication
@EnableCaching
@EnableFeignClients
@EnableConfigurationProperties(GeminiProperties.class)
public class AiServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiServiceApplication.class, args);
	}

}
