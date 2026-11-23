package tn.esprit.skillora.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Body of the provider webhook: POST /api/payments/webhook. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProviderNotification(String transactionReference, ProviderOutcome status, String reason) {
}
