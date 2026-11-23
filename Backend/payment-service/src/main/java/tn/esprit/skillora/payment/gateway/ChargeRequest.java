package tn.esprit.skillora.payment.gateway;

import tn.esprit.skillora.payment.domain.PaymentMethod;

import java.math.BigDecimal;

public record ChargeRequest(Long paymentId, BigDecimal amount, String currency, String description,
                            PaymentMethod method) {
}
