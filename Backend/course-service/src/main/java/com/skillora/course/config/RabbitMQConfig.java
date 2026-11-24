package com.skillora.course.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String SKILLORA_EXCHANGE = "skillora.events";
    public static final String COURSE_PAYMENT_SUCCEEDED_QUEUE = "course.payment.succeeded.queue";
    public static final String PAYMENT_SUCCEEDED_ROUTING_KEY = "payment.succeeded";
    public static final String COURSE_CREATED_ROUTING_KEY = "course.created";

    @Bean
    public TopicExchange skilloraExchange() {
        return new TopicExchange(SKILLORA_EXCHANGE);
    }

    @Bean
    public Queue coursePaymentSucceededQueue() {
        return new Queue(COURSE_PAYMENT_SUCCEEDED_QUEUE, true);
    }

    @Bean
    public Binding coursePaymentSucceededBinding(Queue coursePaymentSucceededQueue, TopicExchange skilloraExchange) {
        return BindingBuilder.bind(coursePaymentSucceededQueue)
                .to(skilloraExchange)
                .with(PAYMENT_SUCCEEDED_ROUTING_KEY);
    }
}
