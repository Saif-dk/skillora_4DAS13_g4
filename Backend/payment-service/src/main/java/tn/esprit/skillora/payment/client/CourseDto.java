package tn.esprit.skillora.payment.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** Subset of the Course service response that the Payment service needs. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CourseDto(Long id, String title, BigDecimal price, String currency, String status) {
}
