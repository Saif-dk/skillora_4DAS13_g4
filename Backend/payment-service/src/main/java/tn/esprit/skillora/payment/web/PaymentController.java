package tn.esprit.skillora.payment.web;

import tn.esprit.skillora.payment.domain.PaymentStatus;
import tn.esprit.skillora.payment.dto.InitiatePaymentRequest;
import tn.esprit.skillora.payment.dto.PageResponse;
import tn.esprit.skillora.payment.dto.PaymentResponse;
import tn.esprit.skillora.payment.dto.PaymentStatusResponse;
import tn.esprit.skillora.payment.dto.RefundRequest;
import tn.esprit.skillora.payment.dto.RefundResponse;
import tn.esprit.skillora.payment.security.CurrentUser;
import tn.esprit.skillora.payment.service.PaymentService;
import tn.esprit.skillora.payment.service.RefundService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;
    private final RefundService refundService;

    public PaymentController(PaymentService paymentService, RefundService refundService) {
        this.paymentService = paymentService;
        this.refundService = refundService;
    }

    /** Initiate a payment (201, 400, 404, 409, 502). */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PaymentResponse> initiate(@Valid @RequestBody InitiatePaymentRequest request,
                                                    Authentication authentication) {
        PaymentResponse response = paymentService.initiate(CurrentUser.from(authentication), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Payment history of the connected user (200, 401). */
    @GetMapping("/me")
    public PageResponse<PaymentResponse> myPayments(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return paymentService.getMyPayments(CurrentUser.from(authentication), pageable);
    }

    /** Payment status (200, 403, 404). */
    @GetMapping("/{id}/status")
    public PaymentStatusResponse status(@PathVariable("id") Long id, Authentication authentication) {
        return paymentService.getStatus(CurrentUser.from(authentication), id);
    }

    /** Full payment details; also used by the Complaint service to check a payment (200, 403, 404). */
    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable("id") Long id, Authentication authentication) {
        return paymentService.getPayment(CurrentUser.from(authentication), id);
    }

    /** Admin follow-up of all payments (200, 403). */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<PaymentResponse> search(
            @RequestParam(name = "status", required = false) PaymentStatus status,
            @RequestParam(name = "studentId", required = false) String studentId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return paymentService.search(status, studentId, pageable);
    }

    /** Refund, full or partial (200, 400, 403, 404, 409, 502). */
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public RefundResponse refund(@PathVariable("id") Long id, @Valid @RequestBody RefundRequest request,
                                 Authentication authentication) {
        return refundService.refund(id, request, authentication.getName());
    }
}
