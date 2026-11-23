package tn.esprit.skillora.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {

    /** ISO currency used when the Course service does not specify one. */
    private String currency = "EUR";

    /** RabbitMQ topic exchange on which payment events are published. */
    private String exchange = "payment.events";

    /** Shared secret used to verify the HMAC-SHA256 signature of provider webhooks. */
    private String webhookSecret = "change-me";

    /** Base URL of the frontend checkout page used by the simulated provider. */
    private String checkoutBaseUrl = "http://localhost:4200/checkout";

    /** A payment still PENDING after this many minutes is marked FAILED so the student can retry. */
    private int pendingExpiryMinutes = 30;

    public int getPendingExpiryMinutes() { return pendingExpiryMinutes; }
    public void setPendingExpiryMinutes(int pendingExpiryMinutes) { this.pendingExpiryMinutes = pendingExpiryMinutes; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
    public String getCheckoutBaseUrl() { return checkoutBaseUrl; }
    public void setCheckoutBaseUrl(String checkoutBaseUrl) { this.checkoutBaseUrl = checkoutBaseUrl; }
}
