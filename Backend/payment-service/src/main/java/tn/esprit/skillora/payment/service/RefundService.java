package tn.esprit.skillora.payment.service;

import tn.esprit.skillora.payment.domain.Invoice;
import tn.esprit.skillora.payment.domain.InvoiceStatus;
import tn.esprit.skillora.payment.domain.Payment;
import tn.esprit.skillora.payment.domain.PaymentStatus;
import tn.esprit.skillora.payment.domain.Refund;
import tn.esprit.skillora.payment.dto.RefundRequest;
import tn.esprit.skillora.payment.dto.RefundResponse;
import tn.esprit.skillora.payment.exception.ConflictException;
import tn.esprit.skillora.payment.exception.ResourceNotFoundException;
import tn.esprit.skillora.payment.gateway.PaymentGateway;
import tn.esprit.skillora.payment.messaging.PaymentEventRecorder;
import tn.esprit.skillora.payment.repository.InvoiceRepository;
import tn.esprit.skillora.payment.repository.PaymentRepository;
import tn.esprit.skillora.payment.repository.RefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
public class RefundService {

    private final PaymentRepository payments;
    private final RefundRepository refunds;
    private final InvoiceRepository invoices;
    private final PaymentGateway gateway;
    private final PaymentEventRecorder events;

    public RefundService(PaymentRepository payments, RefundRepository refunds, InvoiceRepository invoices,
                         PaymentGateway gateway, PaymentEventRecorder events) {
        this.payments = payments;
        this.refunds = refunds;
        this.invoices = invoices;
        this.gateway = gateway;
        this.events = events;
    }

    /**
     * Refunds all or part of a successful payment. The payment row is locked for the duration of the
     * transaction, so two admins cannot refund the same money twice. If the provider call fails the
     * whole transaction is rolled back and nothing is recorded.
     */
    @Transactional
    public RefundResponse refund(Long paymentId, RefundRequest request, String adminId) {
        Payment payment = payments.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment " + paymentId + " not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new ConflictException("Only a successful payment can be refunded (current status: "
                    + payment.getStatus() + ")");
        }

        BigDecimal remaining = payment.getRemainingRefundable();
        BigDecimal amount = request.amount() == null
                ? remaining
                : request.amount().setScale(2, RoundingMode.HALF_UP);
        if (amount.signum() <= 0 || amount.compareTo(remaining) > 0) {
            throw new ConflictException("Refund amount must be between 0.01 and the remaining refundable amount ("
                    + remaining + " " + payment.getCurrency() + ")");
        }

        gateway.refund(payment.getTransactionReference(), amount, payment.getCurrency());

        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setAmount(amount);
        refund.setReason(request.reason());
        refund.setRequestedBy(adminId);
        refund.setRefundDate(Instant.now());
        refund = refunds.save(refund);

        payment.setRefundedAmount(payment.getRefundedAmount().add(amount));
        boolean fullyRefunded = payment.getRefundedAmount().compareTo(payment.getAmount()) >= 0;
        payment.setStatus(fullyRefunded ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED);
        payments.save(payment);

        if (fullyRefunded) {
            invoices.findByPaymentId(paymentId).ifPresent((Invoice invoice) -> invoice.setStatus(InvoiceStatus.REFUNDED));
        }

        events.paymentRefunded(payment, refund, fullyRefunded);
        return RefundResponse.of(refund, payment.getStatus(), payment.getRefundedAmount());
    }
}
