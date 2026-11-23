package tn.esprit.skillora.payment.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    /**
     * Durable topic exchange. Consumers declare their own queues and bind them, e.g.
     * Classroom: "payment.successful", Course: "payment.successful" + "payment.refunded".
     */
    @Bean
    TopicExchange paymentExchange(PaymentProperties properties) {
        return new TopicExchange(properties.getExchange(), true, false);
    }
}
