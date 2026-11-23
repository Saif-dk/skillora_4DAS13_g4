package tn.esprit.skillora.payment.gateway;

import java.math.BigDecimal;

/**
 * Port towards the external payment provider. The platform never sees card numbers: the student pays on
 * the provider's page, and the provider confirms the result through the signed webhook.
 * Implementations must throw {@link tn.esprit.skillora.payment.exception.PaymentProviderException} on any provider failure.
 */
public interface PaymentGateway {

    ChargeSession createCharge(ChargeRequest request);

    void refund(String transactionReference, BigDecimal amount, String currency);
}
