package tn.esprit.skillora.payment.dto;

import tn.esprit.skillora.payment.domain.PaymentMethod;
import jakarta.validation.constraints.NotNull;

/**
 * Note: the amount is deliberately NOT part of the request. It is read from the Course service
 * so a client can never choose its own price.
 */
public record InitiatePaymentRequest(
        @NotNull Long courseId,
        @NotNull PaymentMethod paymentMethod,
        Long enrollmentId) {
}
