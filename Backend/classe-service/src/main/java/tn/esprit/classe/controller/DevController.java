package tn.esprit.classe.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.classe.dto.PaymentData;
import tn.esprit.classe.dto.PaymentSucceededEvent;
import tn.esprit.classe.service.PaymentEventService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/dev")
@Profile("dev")
@RequiredArgsConstructor
public class DevController {

    private final PaymentEventService paymentEventService;

    @PostMapping("/simulate-payment/{enrollmentId}")
    ResponseEntity<Map<String, Object>> simulatePayment(@PathVariable Long enrollmentId) {
        PaymentData data = new PaymentData();
        data.setPaymentId("payment-" + UUID.randomUUID());
        data.setEnrollmentId(enrollmentId);
        data.setStudentId(0L);
        data.setAmount(new BigDecimal("150.00"));
        data.setCurrency("EUR");

        PaymentSucceededEvent event = new PaymentSucceededEvent();
        event.setEventId("event-" + UUID.randomUUID());
        event.setEventType("PaymentSucceeded");
        event.setOccurredAt(Instant.now().toString());
        event.setData(data);

        paymentEventService.process(event);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("processed", true);
        body.put("eventId", event.getEventId());
        body.put("enrollmentId", enrollmentId);
        return ResponseEntity.ok(body);
    }
}
