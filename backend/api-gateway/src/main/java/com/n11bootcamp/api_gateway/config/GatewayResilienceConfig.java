package com.n11bootcamp.api_gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.n11bootcamp.api_gateway.prop.GatewayResilienceProperties;

@Configuration
@EnableConfigurationProperties(GatewayResilienceProperties.class)
public class GatewayResilienceConfig {
}

