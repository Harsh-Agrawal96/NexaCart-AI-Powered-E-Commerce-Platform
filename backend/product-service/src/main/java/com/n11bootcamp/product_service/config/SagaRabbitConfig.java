package com.n11bootcamp.product_service.config;

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
    public Queue inventoryReserveQueue() {
        return QueueBuilder
                .durable(SagaConstants.Q_INVENTORY_RESERVE)
                .withArgument("x-dead-letter-exchange", SagaConstants.SAGA_DLX)
                .build();
    }

    @Bean
    public Queue inventoryReleaseQueue() {
        return QueueBuilder
                .durable(SagaConstants.Q_INVENTORY_RELEASE)
                .withArgument("x-dead-letter-exchange", SagaConstants.SAGA_DLX)
                .build();
    }

    @Bean
    public Queue inventoryConfirmQueue() {
        return QueueBuilder
                .durable(SagaConstants.Q_INVENTORY_CONFIRM)
                .withArgument("x-dead-letter-exchange", SagaConstants.SAGA_DLX)
                .build();
    }

    @Bean
    public Binding bindInventoryReserve() {
        return BindingBuilder
                .bind(inventoryReserveQueue())
                .to(sagaExchange())
                .with(SagaConstants.RK_ORDER_CREATED);
    }

    @Bean
    public Binding bindInventoryRelease() {
        return BindingBuilder
                .bind(inventoryReleaseQueue())
                .to(sagaExchange())
                .with(SagaConstants.RK_PAYMENT_FAILED);
    }

    @Bean
    public Binding bindInventoryConfirm() {
        return BindingBuilder
                .bind(inventoryConfirmQueue())
                .to(sagaExchange())
                .with(SagaConstants.RK_PAYMENT_SUCCEEDED);
    }
}
