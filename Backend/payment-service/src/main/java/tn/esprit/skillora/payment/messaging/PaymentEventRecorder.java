package tn.esprit.skillora.payment.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import tn.esprit.skillora.payment.domain.OutboxEvent;
import tn.esprit.skillora.payment.domain.Payment;
import tn.esprit.skillora.payment.domain.Refund;
import tn.esprit.skillora.payment.dto.PaymentEvent;
import tn.esprit.skillora.payment.dto.PaymentEventData;
import tn.esprit.skillora.payment.repository.OutboxEventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Records domain events in the outbox table, inside the caller's transaction.
 * Event names follow the project presentation: PaymentSuccessful, PaymentFailed, PaymentRefunded.
 */
@Component
public class PaymentEventRecorder {

    public static final String PAYMENT_SUCCESSFUL = "PaymentSuccessful";
    public static final String PAYMENT_FAILED = "PaymentFailed";
    public static final String PAYMENT_REFUNDED = "PaymentRefunded";

    private final OutboxEventRepository outbox;
    private final ObjectMapper objectMapper;

    public PaymentEventRecorder(OutboxEventRepository outbox, ObjectMapper objectMapper) {
        this.outbox = outbox;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void paymentSuccessful(Payment p) {
        store(PAYMENT_SUCCESSFUL, "payment.successful",
                new PaymentEventData(p.getId(), p.getCourseId(), p.getEnrollmentId(), p.getStudentId(),
                        p.getAmount(), p.getCurrency(), null, null, null, null));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void paymentFailed(Payment p, String reason) {
        store(PAYMENT_FAILED, "payment.failed",
                new PaymentEventData(p.getId(), p.getCourseId(), p.getEnrollmentId(), p.getStudentId(),
                        p.getAmount(), p.getCurrency(), reason, null, null, null));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void paymentRefunded(Payment p, Refund refund, boolean fullyRefunded) {
        store(PAYMENT_REFUNDED, "payment.refunded",
                new PaymentEventData(p.getId(), p.getCourseId(), p.getEnrollmentId(), p.getStudentId(),
                        p.getAmount(), p.getCurrency(), null, refund.getId(), refund.getAmount(), fullyRefunded));
    }

    private void store(String eventType, String routingKey, PaymentEventData data) {
        Instant now = Instant.now();
        UUID eventId = UUID.randomUUID();
        try {
            String json = objectMapper.writeValueAsString(new PaymentEvent(eventId, eventType, now, data));
            outbox.save(new OutboxEvent(eventId, eventType, routingKey, json, now));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialise event " + eventType, e);
        }
    }
}
