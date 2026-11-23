package tn.esprit.skillora.payment.messaging;

import tn.esprit.skillora.payment.config.PaymentProperties;
import tn.esprit.skillora.payment.domain.OutboxEvent;
import tn.esprit.skillora.payment.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * Relays outbox rows to RabbitMQ. Delivery is at-least-once: consumers must de-duplicate on "eventId".
 * If the broker is down, rows simply stay in the table and are retried on the next tick.
 */
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository outbox;
    private final RabbitTemplate rabbitTemplate;
    private final PaymentProperties properties;

    public OutboxPublisher(OutboxEventRepository outbox, RabbitTemplate rabbitTemplate, PaymentProperties properties) {
        this.outbox = outbox;
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${payment.outbox.poll-interval-ms:2000}")
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outbox.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
        for (OutboxEvent event : pending) {
            try {
                Message message = MessageBuilder
                        .withBody(event.getPayload().getBytes(StandardCharsets.UTF_8))
                        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                        .setContentEncoding(StandardCharsets.UTF_8.name())
                        .setMessageId(event.getId().toString())
                        .setType(event.getEventType())
                        .build();
                rabbitTemplate.send(properties.getExchange(), event.getRoutingKey(), message);
                event.setPublishedAt(Instant.now());
                log.info("Published {} ({}) with routing key {}", event.getEventType(), event.getId(), event.getRoutingKey());
            } catch (AmqpException e) {
                log.warn("RabbitMQ unavailable, {} event(s) will be retried: {}", pending.size(), e.getMessage());
                return; // keep order: stop at the first failure
            }
        }
    }
}
