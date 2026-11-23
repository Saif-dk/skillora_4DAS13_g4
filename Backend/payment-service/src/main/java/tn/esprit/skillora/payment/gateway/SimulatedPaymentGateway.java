package tn.esprit.skillora.payment.gateway;

import tn.esprit.skillora.payment.config.PaymentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Default provider used during development (the report allows replacing the real provider by a simulation).
 * To integrate a real provider, add another PaymentGateway implementation, e.g.
 * {@code @ConditionalOnProperty(name = "payment.provider", havingValue = "konnect")}, and call its API
 * from createCharge / refund.
 */
@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "simulated", matchIfMissing = true)
public class SimulatedPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(SimulatedPaymentGateway.class);

    private final PaymentProperties properties;

    public SimulatedPaymentGateway(PaymentProperties properties) {
        this.properties = properties;
    }

    @Override
    public ChargeSession createCharge(ChargeRequest request) {
        String reference = "SIM-" + UUID.randomUUID();
        log.info("[simulated provider] charge opened: payment={}, amount={} {}, ref={}",
                request.paymentId(), request.amount(), request.currency(), reference);
        return new ChargeSession(reference, properties.getCheckoutBaseUrl() + "/" + reference);
    }

    @Override
    public void refund(String transactionReference, BigDecimal amount, String currency) {
        log.info("[simulated provider] refund of {} {} accepted for ref={}", amount, currency, transactionReference);
    }
}
