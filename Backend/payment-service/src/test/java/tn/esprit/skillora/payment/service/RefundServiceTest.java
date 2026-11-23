package tn.esprit.skillora.payment.service;

import tn.esprit.skillora.payment.domain.Invoice;
import tn.esprit.skillora.payment.domain.InvoiceStatus;
import tn.esprit.skillora.payment.domain.Payment;
import tn.esprit.skillora.payment.domain.PaymentStatus;
import tn.esprit.skillora.payment.domain.Refund;
import tn.esprit.skillora.payment.dto.RefundRequest;
import tn.esprit.skillora.payment.dto.RefundResponse;
import tn.esprit.skillora.payment.exception.ConflictException;
import tn.esprit.skillora.payment.exception.ResourceNotFoundException;
import tn.esprit.skillora.payment.gateway.PaymentGateway;
import tn.esprit.skillora.payment.messaging.PaymentEventRecorder;
import tn.esprit.skillora.payment.repository.InvoiceRepository;
import tn.esprit.skillora.payment.repository.PaymentRepository;
import tn.esprit.skillora.payment.repository.RefundRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock PaymentRepository payments;
    @Mock RefundRepository refunds;
    @Mock InvoiceRepository invoices;
    @Mock PaymentGateway gateway;
    @Mock PaymentEventRecorder events;
    @InjectMocks RefundService service;

    private Payment payment(PaymentStatus status) {
        Payment p = new Payment();
        ReflectionTestUtils.setField(p, "id", 1L);
        p.setStudentId("u1");
        p.setCourseId(5L);
        p.setAmount(new BigDecimal("100.00"));
        p.setCurrency("EUR");
        p.setTransactionReference("REF");
        p.setStatus(status);
        return p;
    }

    @Test
    void partialRefundKeepsPaymentOpenForFurtherRefunds() {
        Payment p = payment(PaymentStatus.SUCCESS);
        when(payments.findByIdForUpdate(1L)).thenReturn(Optional.of(p));
        when(refunds.save(any(Refund.class))).thenAnswer(inv -> inv.getArgument(0));

        RefundResponse response = service.refund(1L, new RefundRequest(new BigDecimal("40"), "Course not as described"), "admin-1");

        assertEquals(PaymentStatus.PARTIALLY_REFUNDED, p.getStatus());
        assertEquals(0, new BigDecimal("40.00").compareTo(p.getRefundedAmount()));
        assertEquals(PaymentStatus.PARTIALLY_REFUNDED, response.paymentStatus());
        verify(gateway).refund("REF", new BigDecimal("40.00"), "EUR");
        verify(events).paymentRefunded(eq(p), any(Refund.class), eq(false));
    }

    @Test
    void refundWithoutAmountRefundsEverythingAndMarksInvoiceRefunded() {
        Payment p = payment(PaymentStatus.SUCCESS);
        Invoice invoice = new Invoice();
        invoice.setStatus(InvoiceStatus.PAID);
        when(payments.findByIdForUpdate(1L)).thenReturn(Optional.of(p));
        when(refunds.save(any(Refund.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoices.findByPaymentId(1L)).thenReturn(Optional.of(invoice));

        service.refund(1L, new RefundRequest(null, "Duplicate payment"), "admin-1");

        assertEquals(PaymentStatus.REFUNDED, p.getStatus());
        assertEquals(InvoiceStatus.REFUNDED, invoice.getStatus());
        verify(events).paymentRefunded(eq(p), any(Refund.class), eq(true));
    }

    @Test
    void cannotRefundMoreThanRemaining() {
        Payment p = payment(PaymentStatus.SUCCESS);
        when(payments.findByIdForUpdate(1L)).thenReturn(Optional.of(p));

        assertThrows(ConflictException.class,
                () -> service.refund(1L, new RefundRequest(new BigDecimal("100.01"), "too much"), "admin-1"));
        verifyNoInteractions(gateway, events);
    }

    @Test
    void cannotRefundAPaymentThatWasNotSuccessful() {
        Payment p = payment(PaymentStatus.PENDING);
        when(payments.findByIdForUpdate(1L)).thenReturn(Optional.of(p));

        assertThrows(ConflictException.class,
                () -> service.refund(1L, new RefundRequest(null, "reason"), "admin-1"));
        verifyNoInteractions(gateway, events);
    }

    @Test
    void unknownPaymentIsNotFound() {
        when(payments.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.refund(99L, new RefundRequest(null, "reason"), "admin-1"));
    }
}
