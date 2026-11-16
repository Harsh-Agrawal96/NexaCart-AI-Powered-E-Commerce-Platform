package com.n11bootcamp.order_service.config;

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
    public Queue orderCompleteQueue() {
        return QueueBuilder
                .durable(SagaConstants.Q_ORDER_COMPLETE)
                .withArgument("x-dead-letter-exchange", SagaConstants.SAGA_DLX)
                .withArgument("x-dead-letter-routing-key", SagaConstants.Q_ORDER_COMPLETE + ".dlq")
                .build();
    }

    @Bean
    public Queue orderAwaitPaymentQueue() {
        return QueueBuilder
                .durable(SagaConstants.Q_ORDER_AWAIT_PAYMENT)
                .withArgument("x-dead-letter-exchange", SagaConstants.SAGA_DLX)
                .build();
    }

    @Bean
    public Queue orderCancelInventoryQueue() {
        return QueueBuilder
                .durable(SagaConstants.Q_ORDER_CANCEL_INVENTORY)
                .withArgument("x-dead-letter-exchange", SagaConstants.SAGA_DLX)
                .withArgument("x-dead-letter-routing-key", SagaConstants.Q_ORDER_CANCEL_INVENTORY + ".dlq")
                .build();
    }

    @Bean
    public Queue orderCancelPaymentQueue() {
        return QueueBuilder
                .durable(SagaConstants.Q_ORDER_CANCEL_PAYMENT)
                .withArgument("x-dead-letter-exchange", SagaConstants.SAGA_DLX)
                .withArgument("x-dead-letter-routing-key", SagaConstants.Q_ORDER_CANCEL_PAYMENT + ".dlq")
                .build();
    }

    @Bean
    public Binding bindingInventoryFailed() {
        return BindingBuilder
                .bind(orderCancelInventoryQueue())
                .to(sagaExchange())
                .with(SagaConstants.RK_INVENTORY_FAILED);
    }

    @Bean
    public Binding bindingPaymentFailed() {
        return BindingBuilder
                .bind(orderCancelPaymentQueue())
                .to(sagaExchange())
                .with(SagaConstants.RK_PAYMENT_FAILED);
    }

    @Bean
    public Binding bindingPaymentSucceeded() {
        return BindingBuilder
                .bind(orderCompleteQueue())
                .to(sagaExchange())
                .with(SagaConstants.RK_PAYMENT_SUCCEEDED);
    }

    @Bean
    public Binding orderAwaitPaymentBinding() {
        return BindingBuilder
                .bind(orderAwaitPaymentQueue())
                .to(sagaExchange())
                .with(SagaConstants.RK_INVENTORY_RESERVED);
    }
}
