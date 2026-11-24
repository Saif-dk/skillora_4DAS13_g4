package tn.esprit.classe.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "payment.exchange";
    public static final String QUEUE = "classe.payment.succeeded.queue";
    public static final String ROUTING_KEY = "payment.succeeded";

    @Bean
    TopicExchange paymentExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue classePaymentSucceededQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    Binding classePaymentSucceededBinding() {
        return BindingBuilder.bind(classePaymentSucceededQueue())
                .to(paymentExchange())
                .with(ROUTING_KEY);
    }

    @Bean
    MessageConverter jacksonJsonMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}
