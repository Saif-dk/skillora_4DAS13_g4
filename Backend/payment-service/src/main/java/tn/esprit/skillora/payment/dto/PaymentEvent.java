package tn.esprit.skillora.payment.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Envelope of every event published on the "payment.events" exchange.
 * Consumers should use {@code eventId} to ignore duplicates (delivery is at-least-once).
 */
public record PaymentEvent(UUID eventId, String eventType, Instant occurredAt, PaymentEventData data) {
}
