package tn.esprit.skillora.payment.service;

import tn.esprit.skillora.payment.config.PaymentProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebhookSignatureVerifierTest {

    private WebhookSignatureVerifier verifier;

    @BeforeEach
    void setUp() {
        PaymentProperties properties = new PaymentProperties();
        properties.setWebhookSecret("key");
        verifier = new WebhookSignatureVerifier(properties);
    }

    @Test
    void producesTheStandardHmacSha256() {
        // well-known HMAC-SHA256 test vector (key = "key")
        byte[] body = "The quick brown fox jumps over the lazy dog".getBytes(StandardCharsets.UTF_8);
        assertEquals("f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8", verifier.sign(body));
    }

    @Test
    void acceptsCorrectSignatureWithOrWithoutPrefix() {
        byte[] body = "{\"status\":\"SUCCESS\"}".getBytes(StandardCharsets.UTF_8);
        String signature = verifier.sign(body);
        assertTrue(verifier.isValid(body, signature));
        assertTrue(verifier.isValid(body, "sha256=" + signature));
    }

    @Test
    void rejectsTamperedBodyAndMissingSignature() {
        byte[] body = "{\"status\":\"SUCCESS\"}".getBytes(StandardCharsets.UTF_8);
        byte[] tampered = "{\"status\":\"FAILED\"}".getBytes(StandardCharsets.UTF_8);
        assertFalse(verifier.isValid(tampered, verifier.sign(body)));
        assertFalse(verifier.isValid(body, null));
        assertFalse(verifier.isValid(body, " "));
    }
}
