package tn.esprit.skillora.payment.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import tn.esprit.skillora.payment.dto.ProviderNotification;
import tn.esprit.skillora.payment.exception.BadRequestException;
import tn.esprit.skillora.payment.service.PaymentSettlementService;
import tn.esprit.skillora.payment.service.WebhookSignatureVerifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/** Public endpoint called by the payment provider (200, 400). Authenticated by HMAC signature, not JWT. */
@RestController
@RequestMapping("/api/payments")
public class WebhookController {

    private final WebhookSignatureVerifier signatureVerifier;
    private final PaymentSettlementService settlement;
    private final ObjectMapper objectMapper;

    public WebhookController(WebhookSignatureVerifier signatureVerifier, PaymentSettlementService settlement,
                             ObjectMapper objectMapper) {
        this.signatureVerifier = signatureVerifier;
        this.settlement = settlement;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> receive(
            @RequestBody byte[] body,
            @RequestHeader(name = WebhookSignatureVerifier.HEADER, required = false) String signature) {

        // Verify the signature on the RAW bytes, before parsing anything.
        if (!signatureVerifier.isValid(body, signature)) {
            throw new BadRequestException("Invalid webhook signature");
        }
        ProviderNotification notification;
        try {
            notification = objectMapper.readValue(body, ProviderNotification.class);
        } catch (IOException e) {
            throw new BadRequestException("Malformed webhook body");
        }
        settlement.settle(notification.transactionReference(), notification.status(), notification.reason());
        return ResponseEntity.ok(Map.of("received", true));
    }
}
