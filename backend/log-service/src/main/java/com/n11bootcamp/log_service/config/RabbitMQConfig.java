package com.n11bootcamp.log_service.config;

import com.n11bootcamp.common.constant.SagaConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String Q_LOG_ORDER_CREATED      = "q.log.order.created";
    public static final String Q_LOG_INVENTORY_RESERVED = "q.log.inventory.reserved";
    public static final String Q_LOG_INVENTORY_FAILED   = "q.log.inventory.failed";
    public static final String Q_LOG_PAYMENT_SUCCEEDED  = "q.log.payment.succeeded";
    public static final String Q_LOG_PAYMENT_FAILED     = "q.log.payment.failed";
    public static final String Q_LOG_USER_BEHAVIOR      = "q.log.user.behavior";

    // Exchange
    @Bean
    public TopicExchange sagaExchange() {
        return ExchangeBuilder
                .topicExchange(SagaConstants.SAGA_EXCHANGE)
                .durable(true)
                .build();
    }

    // Queues
    @Bean
    public Queue logOrderCreatedQueue() {
        return QueueBuilder.durable(Q_LOG_ORDER_CREATED).build();
    }

    @Bean
    public Queue logInventoryReservedQueue() {
        return QueueBuilder.durable(Q_LOG_INVENTORY_RESERVED).build();
    }

    @Bean
    public Queue logInventoryFailedQueue() {
        return QueueBuilder.durable(Q_LOG_INVENTORY_FAILED).build();
    }

    @Bean
    public Queue logPaymentSucceededQueue() {
        return QueueBuilder.durable(Q_LOG_PAYMENT_SUCCEEDED).build();
    }

    @Bean
    public Queue logPaymentFailedQueue() {
        return QueueBuilder.durable(Q_LOG_PAYMENT_FAILED).build();
    }

    @Bean
    public Queue logUserBehaviorQueue() {
        return QueueBuilder.durable(Q_LOG_USER_BEHAVIOR).build();
    }

    // Bindings
    @Bean
    public Binding bindLogOrderCreated() {
        return BindingBuilder
                .bind(logOrderCreatedQueue())
                .to(sagaExchange())
                .with(SagaConstants.RK_ORDER_CREATED);
    }

    @Bean
    public Binding bindLogInventoryReserved() {
        return BindingBuilder
                .bind(logInventoryReservedQueue())
                .to(sagaExchange())
                .with(SagaConstants.RK_INVENTORY_RESERVED);
    }

    @Bean
    public Binding bindLogInventoryFailed() {
        return BindingBuilder
                .bind(logInventoryFailedQueue())
                .to(sagaExchange())
                .with(SagaConstants.RK_INVENTORY_FAILED);
    }

    @Bean
    public Binding bindLogPaymentSucceeded() {
        return BindingBuilder
                .bind(logPaymentSucceededQueue())
                .to(sagaExchange())
                .with(SagaConstants.RK_PAYMENT_SUCCEEDED);
    }

    @Bean
    public Binding bindLogPaymentFailed() {
        return BindingBuilder
                .bind(logPaymentFailedQueue())
                .to(sagaExchange())
                .with(SagaConstants.RK_PAYMENT_FAILED);
    }

    @Bean
    public Binding bindLogUserBehavior() {
        return BindingBuilder
                .bind(logUserBehaviorQueue())
                .to(sagaExchange())
                .with(SagaConstants.RK_USER_BEHAVIOR_PATTERN);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    // JSON converter
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setMissingQueuesFatal(false);
        return factory;
    }
}