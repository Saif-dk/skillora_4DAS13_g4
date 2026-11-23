package tn.esprit.skillora.payment.service;

import tn.esprit.skillora.payment.config.PaymentProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;

/**
 * The webhook is public (the provider has no Keycloak token), so it is authenticated with a shared secret:
 * the provider sends header X-Payment-Signature = hex(HMAC-SHA256(secret, rawBody)).
 */
@Component
public class WebhookSignatureVerifier {

    public static final String HEADER = "X-Payment-Signature";
    private static final String ALGORITHM = "HmacSHA256";
    private static final String PREFIX = "sha256=";

    private final PaymentProperties properties;

    public WebhookSignatureVerifier(PaymentProperties properties) {
        this.properties = properties;
    }

    public boolean isValid(byte[] body, String signatureHeader) {
        if (body == null || signatureHeader == null || signatureHeader.isBlank()) {
            return false;
        }
        String provided = signatureHeader.trim().toLowerCase(Locale.ROOT);
        if (provided.startsWith(PREFIX)) {
            provided = provided.substring(PREFIX.length());
        }
        // constant-time comparison
        return MessageDigest.isEqual(
                sign(body).getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8));
    }

    public String sign(byte[] body) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(properties.getWebhookSecret().getBytes(StandardCharsets.UTF_8), ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(body));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot compute webhook signature", e);
        }
    }
}
