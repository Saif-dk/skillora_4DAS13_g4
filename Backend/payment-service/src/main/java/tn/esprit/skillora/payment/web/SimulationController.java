package tn.esprit.skillora.payment.web;

import tn.esprit.skillora.payment.dto.ProviderOutcome;
import tn.esprit.skillora.payment.service.PaymentSettlementService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * DEV ONLY (profile "dev"): plays the role of the payment provider so the whole flow can be demonstrated
 * without a real provider or a signed webhook. The bean does not exist in any other profile.
 */
@Profile("dev")
@RestController
@RequestMapping("/api/dev/payments")
@SecurityRequirement(name = "bearerAuth")
public class SimulationController {

    private final PaymentSettlementService settlement;

    public SimulationController(PaymentSettlementService settlement) {
        this.settlement = settlement;
    }

    @PostMapping("/{reference}/simulate")
    public ResponseEntity<Map<String, Object>> simulate(
            @PathVariable("reference") String reference,
            @RequestParam(name = "outcome", defaultValue = "SUCCESS") ProviderOutcome outcome) {
        settlement.settle(reference, outcome, "Simulated failure");
        return ResponseEntity.ok(Map.of("reference", reference, "outcome", outcome));
    }
}
