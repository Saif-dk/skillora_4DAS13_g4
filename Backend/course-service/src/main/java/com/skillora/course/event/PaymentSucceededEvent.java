package com.skillora.course.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentSucceededEvent(
        String eventId,
        Long paymentId,
        Long enrollmentId,
        Long studentId,
        Long courseId,
        BigDecimal amount,
        String currency,
        LocalDateTime occurredAt
) {
}
