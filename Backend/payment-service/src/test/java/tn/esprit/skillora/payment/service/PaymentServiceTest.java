package tn.esprit.skillora.payment.service;

import tn.esprit.skillora.payment.client.CourseCatalog;
import tn.esprit.skillora.payment.client.CourseDto;
import tn.esprit.skillora.payment.config.PaymentProperties;
import tn.esprit.skillora.payment.domain.Payment;
import tn.esprit.skillora.payment.domain.PaymentMethod;
import tn.esprit.skillora.payment.domain.PaymentStatus;
import tn.esprit.skillora.payment.dto.InitiatePaymentRequest;
import tn.esprit.skillora.payment.dto.PaymentResponse;
import tn.esprit.skillora.payment.exception.ConflictException;
import tn.esprit.skillora.payment.exception.PaymentProviderException;
import tn.esprit.skillora.payment.gateway.ChargeSession;
import tn.esprit.skillora.payment.gateway.PaymentGateway;
import tn.esprit.skillora.payment.repository.InvoiceRepository;
import tn.esprit.skillora.payment.repository.PaymentRepository;
import tn.esprit.skillora.payment.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock PaymentRepository payments;
    @Mock InvoiceRepository invoices;
    @Mock CourseCatalog courseCatalog;
    @Mock PaymentGateway gateway;
    @Mock PaymentSettlementService settlement;

    private PaymentService service;
    private final CurrentUser student = new CurrentUser("u1", "Alice", false);
    private final InitiatePaymentRequest request = new InitiatePaymentRequest(5L, PaymentMethod.CARD, null);
    private final CourseDto course = new CourseDto(5L, "Java", new BigDecimal("150"), null, "PUBLISHED");

    @BeforeEach
    void setUp() {
        service = new PaymentService(payments, invoices, courseCatalog, gateway, settlement, new PaymentProperties());
    }

    @Test
    void initiateCreatesPendingPaymentWithPriceTakenFromCourse() {
        when(courseCatalog.getPayableCourse(5L)).thenReturn(course);
        when(payments.existsByStudentIdAndCourseIdAndStatusIn(eq("u1"), eq(5L), any())).thenReturn(false);
        when(payments.saveAndFlush(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", 10L);
            return p;
        });
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(gateway.createCharge(any())).thenReturn(new ChargeSession("SIM-1", "http://pay/SIM-1"));

        PaymentResponse response = service.initiate(student, request);

        assertEquals(PaymentStatus.PENDING, response.status());
        assertEquals(0, new BigDecimal("150.00").compareTo(response.amount()));
        assertEquals("EUR", response.currency());
        assertEquals("SIM-1", response.transactionReference());
        assertEquals("http://pay/SIM-1", response.checkoutUrl());
        assertEquals("u1", response.studentId());
    }

    @Test
    void initiateRefusesASecondLivePaymentForTheSameCourse() {
        when(courseCatalog.getPayableCourse(5L)).thenReturn(course);
        when(payments.existsByStudentIdAndCourseIdAndStatusIn(eq("u1"), eq(5L), any())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.initiate(student, request));
        verify(payments, never()).saveAndFlush(any(Payment.class));
        verify(gateway, never()).createCharge(any());
    }

    @Test
    void providerFailureMarksPaymentFailedAndIsPropagated() {
        when(courseCatalog.getPayableCourse(5L)).thenReturn(course);
        when(payments.existsByStudentIdAndCourseIdAndStatusIn(eq("u1"), eq(5L), any())).thenReturn(false);
        when(payments.saveAndFlush(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", 10L);
            return p;
        });
        when(gateway.createCharge(any())).thenThrow(new PaymentProviderException("boom"));

        assertThrows(PaymentProviderException.class, () -> service.initiate(student, request));
        verify(settlement).markFailed(eq(10L), contains("boom"));
    }

    @Test
    void aStudentCannotReadSomeoneElsesPayment() {
        Payment p = new Payment();
        ReflectionTestUtils.setField(p, "id", 7L);
        p.setStudentId("u1");
        p.setStatus(PaymentStatus.SUCCESS);
        when(payments.findById(7L)).thenReturn(Optional.of(p));

        CurrentUser other = new CurrentUser("u2", "Bob", false);
        CurrentUser admin = new CurrentUser("a1", "Admin", true);

        assertThrows(AccessDeniedException.class, () -> service.getStatus(other, 7L));
        assertEquals(PaymentStatus.SUCCESS, service.getStatus(admin, 7L).status());
        assertEquals(PaymentStatus.SUCCESS, service.getStatus(student, 7L).status());
    }
}
