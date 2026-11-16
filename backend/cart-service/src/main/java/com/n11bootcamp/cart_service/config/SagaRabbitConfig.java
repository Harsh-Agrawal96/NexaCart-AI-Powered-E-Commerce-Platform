package com.n11bootcamp.cart_service.config;

import com.n11bootcamp.common.constant.SagaConstants;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SagaRabbitConfig {

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

    @Bean
    public Queue cartClearQueue() {
        return QueueBuilder
                .durable(SagaConstants.Q_CART_CLEAR)
                .withArgument("x-dead-letter-exchange", SagaConstants.SAGA_DLX)
                .build();
    }

    @Bean
    public Binding cartClearBinding() {
        return BindingBuilder
                .bind(cartClearQueue())
                .to(sagaExchange())
                .with(SagaConstants.RK_PAYMENT_SUCCEEDED);
    }
}
