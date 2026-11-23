package tn.esprit.skillora.payment.service;

import tn.esprit.skillora.payment.domain.Payment;
import tn.esprit.skillora.payment.domain.PaymentStatus;
import tn.esprit.skillora.payment.dto.ProviderOutcome;
import tn.esprit.skillora.payment.exception.BadRequestException;
import tn.esprit.skillora.payment.messaging.PaymentEventRecorder;
import tn.esprit.skillora.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Applies the final outcome of a payment (success / failure) atomically:
 * status change + invoice + outbox event happen in ONE database transaction.
 */
@Service
public class PaymentSettlementService {

    private static final Logger log = LoggerFactory.getLogger(PaymentSettlementService.class);
    private static final int MAX_REASON_LENGTH = 500;

    private final PaymentRepository payments;
    private final InvoiceService invoiceService;
    private final PaymentEventRecorder events;

    public PaymentSettlementService(PaymentRepository payments, InvoiceService invoiceService,
                                    PaymentEventRecorder events) {
        this.payments = payments;
        this.invoiceService = invoiceService;
        this.events = events;
    }

    /** Handles the provider confirmation (webhook). Idempotent: replays are ignored. */
    @Transactional
    public void settle(String transactionReference, ProviderOutcome outcome, String reason) {
        if (transactionReference == null || transactionReference.isBlank() || outcome == null) {
            throw new BadRequestException("transactionReference and status are required");
        }
        Payment payment = payments.findByTransactionReferenceForUpdate(transactionReference)
                .orElseThrow(() -> new BadRequestException("Unknown transaction reference"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            if (outcome == ProviderOutcome.SUCCESS && payment.getStatus() != PaymentStatus.SUCCESS
                    && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
                log.warn("Provider reports SUCCESS for payment {} which is already {}: manual reconciliation needed",
                        payment.getId(), payment.getStatus());
            } else {
                log.info("Ignoring duplicate notification for payment {} (already {})", payment.getId(), payment.getStatus());
            }
            return;
        }

        if (outcome == ProviderOutcome.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaymentDate(Instant.now());
            payments.save(payment);
            invoiceService.issueFor(payment);
            events.paymentSuccessful(payment);
        } else {
            fail(payment, reason == null || reason.isBlank() ? "Payment declined by the provider" : reason);
        }
    }

    /** Marks a PENDING payment as FAILED (provider error at initiation, or expiry). No-op otherwise. */
    @Transactional
    public void markFailed(Long paymentId, String reason) {
        payments.findByIdForUpdate(paymentId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.PENDING) {
                fail(payment, reason);
            }
        });
    }

    private void fail(Payment payment, String reason) {
        String trimmed = reason.length() > MAX_REASON_LENGTH ? reason.substring(0, MAX_REASON_LENGTH) : reason;
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(trimmed);
        payments.save(payment);
        events.paymentFailed(payment, trimmed);
    }
}
