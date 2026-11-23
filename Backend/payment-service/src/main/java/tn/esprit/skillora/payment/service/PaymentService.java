package tn.esprit.skillora.payment.service;

import tn.esprit.skillora.payment.client.CourseCatalog;
import tn.esprit.skillora.payment.client.CourseDto;
import tn.esprit.skillora.payment.config.PaymentProperties;
import tn.esprit.skillora.payment.domain.Invoice;
import tn.esprit.skillora.payment.domain.Payment;
import tn.esprit.skillora.payment.domain.PaymentStatus;
import tn.esprit.skillora.payment.dto.InitiatePaymentRequest;
import tn.esprit.skillora.payment.dto.PageResponse;
import tn.esprit.skillora.payment.dto.PaymentResponse;
import tn.esprit.skillora.payment.dto.PaymentStatusResponse;
import tn.esprit.skillora.payment.exception.ConflictException;
import tn.esprit.skillora.payment.exception.PaymentProviderException;
import tn.esprit.skillora.payment.exception.ResourceNotFoundException;
import tn.esprit.skillora.payment.gateway.ChargeRequest;
import tn.esprit.skillora.payment.gateway.ChargeSession;
import tn.esprit.skillora.payment.gateway.PaymentGateway;
import tn.esprit.skillora.payment.repository.InvoiceRepository;
import tn.esprit.skillora.payment.repository.PaymentRepository;
import tn.esprit.skillora.payment.security.CurrentUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class PaymentService {

    /** Statuses that mean "the student already has (or is about to have) this course". */
    static final Set<PaymentStatus> LIVE_STATUSES =
            EnumSet.of(PaymentStatus.PENDING, PaymentStatus.SUCCESS, PaymentStatus.PARTIALLY_REFUNDED);

    private final PaymentRepository payments;
    private final InvoiceRepository invoices;
    private final CourseCatalog courseCatalog;
    private final PaymentGateway gateway;
    private final PaymentSettlementService settlement;
    private final PaymentProperties properties;

    public PaymentService(PaymentRepository payments, InvoiceRepository invoices, CourseCatalog courseCatalog,
                          PaymentGateway gateway, PaymentSettlementService settlement, PaymentProperties properties) {
        this.payments = payments;
        this.invoices = invoices;
        this.courseCatalog = courseCatalog;
        this.gateway = gateway;
        this.settlement = settlement;
        this.properties = properties;
    }

    /**
     * Starts a payment. Deliberately NOT @Transactional: no DB transaction is held open while we wait for
     * the Course service or the payment provider (remote calls).
     */
    public PaymentResponse initiate(CurrentUser user, InitiatePaymentRequest request) {
        // 1. synchronous call to Course service: existence, published, price
        CourseDto course = courseCatalog.getPayableCourse(request.courseId());

        // 2. business rule: no second live payment for the same course
        if (payments.existsByStudentIdAndCourseIdAndStatusIn(user.id(), course.id(), LIVE_STATUSES)) {
            throw new ConflictException("You already have a pending or completed payment for this course");
        }

        Payment payment = new Payment();
        payment.setStudentId(user.id());
        payment.setStudentName(user.name());
        payment.setCourseId(course.id());
        payment.setCourseTitle(course.title());
        payment.setEnrollmentId(request.enrollmentId());
        payment.setAmount(course.price().setScale(2, RoundingMode.HALF_UP));
        payment.setCurrency(course.currency() == null || course.currency().isBlank()
                ? properties.getCurrency()
                : course.currency().toUpperCase(Locale.ROOT));
        payment.setPaymentMethod(request.paymentMethod());
        payment.setStatus(PaymentStatus.PENDING);

        try {
            payment = payments.saveAndFlush(payment);   // the partial unique index also guards concurrent requests
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("You already have a pending or completed payment for this course");
        }

        // 3. open the payment at the provider
        ChargeSession session;
        try {
            session = gateway.createCharge(new ChargeRequest(payment.getId(), payment.getAmount(),
                    payment.getCurrency(), "Course: " + course.title(), request.paymentMethod()));
        } catch (RuntimeException e) {
            settlement.markFailed(payment.getId(), "Provider error: " + e.getMessage());
            if (e instanceof PaymentProviderException) {
                throw e;
            }
            throw new PaymentProviderException("Payment provider error", e);
        }

        payment.setTransactionReference(session.transactionReference());
        payment = payments.save(payment);
        return PaymentResponse.of(payment, null, session.checkoutUrl());
    }

    @Transactional(readOnly = true)
    public PaymentStatusResponse getStatus(CurrentUser user, Long paymentId) {
        Payment payment = findAccessible(user, paymentId);
        return new PaymentStatusResponse(payment.getId(), payment.getStatus(), payment.getUpdatedAt());
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(CurrentUser user, Long paymentId) {
        Payment payment = findAccessible(user, paymentId);
        Long invoiceId = invoices.findByPaymentId(paymentId).map(Invoice::getId).orElse(null);
        return PaymentResponse.of(payment, invoiceId, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getMyPayments(CurrentUser user, Pageable pageable) {
        return toPage(payments.findByStudentId(user.id(), pageable));
    }

    /** Admin follow-up of all payments, optionally filtered. */
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> search(PaymentStatus status, String studentId, Pageable pageable) {
        Specification<Payment> spec = (root, query, cb) -> cb.conjunction();
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (studentId != null && !studentId.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("studentId"), studentId));
        }
        return toPage(payments.findAll(spec, pageable));
    }

    private Payment findAccessible(CurrentUser user, Long paymentId) {
        Payment payment = payments.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment " + paymentId + " not found"));
        if (!user.admin() && !payment.getStudentId().equals(user.id())) {
            throw new AccessDeniedException("You are not allowed to access this payment");
        }
        return payment;
    }

    private PageResponse<PaymentResponse> toPage(Page<Payment> page) {
        List<Long> ids = page.getContent().stream().map(Payment::getId).toList();
        Map<Long, Long> invoiceIdByPayment = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Invoice invoice : invoices.findByPaymentIdIn(ids)) {
                invoiceIdByPayment.put(invoice.getPayment().getId(), invoice.getId());
            }
        }
        return PageResponse.of(page, p -> PaymentResponse.of(p, invoiceIdByPayment.get(p.getId()), null));
    }
}
