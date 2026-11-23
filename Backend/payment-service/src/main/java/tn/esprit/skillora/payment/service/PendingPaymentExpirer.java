package tn.esprit.skillora.payment.service;

import tn.esprit.skillora.payment.config.PaymentProperties;
import tn.esprit.skillora.payment.domain.Payment;
import tn.esprit.skillora.payment.domain.PaymentStatus;
import tn.esprit.skillora.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Students who open a checkout and never pay would otherwise be blocked by the "one live payment per course"
 * rule forever. Stale PENDING payments are therefore marked FAILED, which lets the student start again.
 */
@Component
public class PendingPaymentExpirer {

    private static final Logger log = LoggerFactory.getLogger(PendingPaymentExpirer.class);

    private final PaymentRepository payments;
    private final PaymentSettlementService settlement;
    private final PaymentProperties properties;

    public PendingPaymentExpirer(PaymentRepository payments, PaymentSettlementService settlement,
                                 PaymentProperties properties) {
        this.payments = payments;
        this.settlement = settlement;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${payment.expiry-check-interval-ms:60000}")
    public void expireStalePayments() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(properties.getPendingExpiryMinutes()));
        List<Payment> stale = payments.findByStatusAndCreatedAtBefore(PaymentStatus.PENDING, cutoff);
        for (Payment payment : stale) {
            settlement.markFailed(payment.getId(), "Payment expired before completion");
        }
        if (!stale.isEmpty()) {
            log.info("Expired {} stale pending payment(s)", stale.size());
        }
    }
}
