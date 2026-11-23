package tn.esprit.skillora.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentEventData(
        Long paymentId,
        Long courseId,
        Long enrollmentId,
        String studentId,
        BigDecimal amount,
        String currency,
        String failureReason,
        Long refundId,
        BigDecimal refundedAmount,
        Boolean fullyRefunded) {
}
