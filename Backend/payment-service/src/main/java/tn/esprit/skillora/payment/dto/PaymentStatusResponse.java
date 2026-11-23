package tn.esprit.skillora.payment.dto;

import tn.esprit.skillora.payment.domain.PaymentStatus;

import java.time.Instant;

public record PaymentStatusResponse(Long paymentId, PaymentStatus status, Instant updatedAt) {
}
