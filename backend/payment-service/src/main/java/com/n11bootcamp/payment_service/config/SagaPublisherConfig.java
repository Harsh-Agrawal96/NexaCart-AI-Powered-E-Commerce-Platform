package com.n11bootcamp.payment_service.config;

import com.n11bootcamp.common.constant.SagaConstants;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SagaPublisherConfig {

    @Bean
    public TopicExchange sagaExchange() {
        return ExchangeBuilder
                .topicExchange(SagaConstants.SAGA_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange sagaDlx() {
        return ExchangeBuilder
                .directExchange(SagaConstants.SAGA_DLX)
                .durable(true)
                .build();
    }
}
