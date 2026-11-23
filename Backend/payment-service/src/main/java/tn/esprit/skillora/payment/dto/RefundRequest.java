package tn.esprit.skillora.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** {@code amount} is optional: when absent, the whole remaining amount is refunded. */
public record RefundRequest(
        @Positive BigDecimal amount,
        @NotBlank @Size(max = 500) String reason) {
}
