package tn.esprit.skillora.payment.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import tn.esprit.skillora.payment.config.PaymentProperties;
import tn.esprit.skillora.payment.dto.ProviderOutcome;
import tn.esprit.skillora.payment.service.PaymentSettlementService;
import tn.esprit.skillora.payment.service.WebhookSignatureVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebhookControllerTest {

    private PaymentSettlementService settlement;
    private WebhookSignatureVerifier verifier;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        PaymentProperties properties = new PaymentProperties();
        properties.setWebhookSecret("test-secret");
        verifier = new WebhookSignatureVerifier(properties);
        settlement = mock(PaymentSettlementService.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new WebhookController(verifier, settlement, new ObjectMapper()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void validSignatureSettlesThePayment() throws Exception {
        String body = "{\"transactionReference\":\"REF\",\"status\":\"SUCCESS\"}";
        String signature = verifier.sign(body.getBytes(StandardCharsets.UTF_8));

        mvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(WebhookSignatureVerifier.HEADER, signature)
                        .content(body))
                .andExpect(status().isOk());

        verify(settlement).settle("REF", ProviderOutcome.SUCCESS, null);
    }

    @Test
    void invalidSignatureIsRejectedWith400AndNothingIsSettled() throws Exception {
        mvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(WebhookSignatureVerifier.HEADER, "deadbeef")
                        .content("{\"transactionReference\":\"REF\",\"status\":\"SUCCESS\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(settlement);
    }

    @Test
    void malformedBodyWithValidSignatureIs400() throws Exception {
        String body = "not json";
        String signature = verifier.sign(body.getBytes(StandardCharsets.UTF_8));

        mvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(WebhookSignatureVerifier.HEADER, signature)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(settlement);
    }
}
