package tn.esprit.skillora.payment.dto;

import tn.esprit.skillora.payment.domain.PaymentStatus;
import tn.esprit.skillora.payment.domain.Refund;

import java.math.BigDecimal;
import java.time.Instant;

public record RefundResponse(
        Long id,
        Long paymentId,
        BigDecimal amount,
        String reason,
        Instant refundDate,
        PaymentStatus paymentStatus,
        BigDecimal totalRefunded) {

    public static RefundResponse of(Refund refund, PaymentStatus paymentStatus, BigDecimal totalRefunded) {
        return new RefundResponse(refund.getId(), refund.getPayment().getId(), refund.getAmount(),
                refund.getReason(), refund.getRefundDate(), paymentStatus, totalRefunded);
    }
}
