package tn.esprit.skillora.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import tn.esprit.skillora.payment.domain.Payment;
import tn.esprit.skillora.payment.domain.PaymentMethod;
import tn.esprit.skillora.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(
        Long id,
        Long courseId,
        String courseTitle,
        Long enrollmentId,
        String studentId,
        BigDecimal amount,
        BigDecimal refundedAmount,
        String currency,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        String transactionReference,
        String failureReason,
        Long invoiceId,
        Instant createdAt,
        Instant paymentDate,
        String checkoutUrl) {

    public static PaymentResponse of(Payment p, Long invoiceId, String checkoutUrl) {
        return new PaymentResponse(
                p.getId(), p.getCourseId(), p.getCourseTitle(), p.getEnrollmentId(), p.getStudentId(),
                p.getAmount(), p.getRefundedAmount(), p.getCurrency(), p.getPaymentMethod(), p.getStatus(),
                p.getTransactionReference(), p.getFailureReason(), invoiceId,
                p.getCreatedAt(), p.getPaymentDate(), checkoutUrl);
    }
}
