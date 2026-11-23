package tn.esprit.skillora.payment.gateway;

/** What the provider returns when a payment is opened: its own reference + where the student pays. */
public record ChargeSession(String transactionReference, String checkoutUrl) {
}
