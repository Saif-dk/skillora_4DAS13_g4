package tn.esprit.skillora.payment.service;

import tn.esprit.skillora.payment.domain.Payment;
import tn.esprit.skillora.payment.domain.PaymentStatus;
import tn.esprit.skillora.payment.dto.ProviderOutcome;
import tn.esprit.skillora.payment.exception.BadRequestException;
import tn.esprit.skillora.payment.messaging.PaymentEventRecorder;
import tn.esprit.skillora.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentSettlementServiceTest {

    @Mock PaymentRepository payments;
    @Mock InvoiceService invoiceService;
    @Mock PaymentEventRecorder events;
    @InjectMocks PaymentSettlementService service;

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
    void successConfirmsPaymentIssuesInvoiceAndRecordsEvent() {
        Payment p = payment(PaymentStatus.PENDING);
        when(payments.findByTransactionReferenceForUpdate("REF")).thenReturn(Optional.of(p));

        service.settle("REF", ProviderOutcome.SUCCESS, null);

        assertEquals(PaymentStatus.SUCCESS, p.getStatus());
        assertNotNull(p.getPaymentDate());
        verify(invoiceService).issueFor(p);
        verify(events).paymentSuccessful(p);
    }

    @Test
    void duplicateNotificationIsIgnored() {
        Payment p = payment(PaymentStatus.SUCCESS);
        when(payments.findByTransactionReferenceForUpdate("REF")).thenReturn(Optional.of(p));

        service.settle("REF", ProviderOutcome.SUCCESS, null);

        verifyNoInteractions(invoiceService, events);
        verify(payments, never()).save(any(Payment.class));
    }

    @Test
    void failureStoresReasonAndRecordsEventWithoutInvoice() {
        Payment p = payment(PaymentStatus.PENDING);
        when(payments.findByTransactionReferenceForUpdate("REF")).thenReturn(Optional.of(p));

        service.settle("REF", ProviderOutcome.FAILED, "Card declined");

        assertEquals(PaymentStatus.FAILED, p.getStatus());
        assertEquals("Card declined", p.getFailureReason());
        verify(events).paymentFailed(p, "Card declined");
        verifyNoInteractions(invoiceService);
    }

    @Test
    void unknownReferenceIsABadRequest() {
        when(payments.findByTransactionReferenceForUpdate("NOPE")).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> service.settle("NOPE", ProviderOutcome.SUCCESS, null));
    }

    @Test
    void markFailedOnlyAffectsPendingPayments() {
        Payment pending = payment(PaymentStatus.PENDING);
        when(payments.findByIdForUpdate(1L)).thenReturn(Optional.of(pending));
        service.markFailed(1L, "expired");
        assertEquals(PaymentStatus.FAILED, pending.getStatus());
        verify(events).paymentFailed(pending, "expired");
    }

    @Test
    void markFailedIsNoOpOnCompletedPayment() {
        Payment done = payment(PaymentStatus.SUCCESS);
        when(payments.findByIdForUpdate(1L)).thenReturn(Optional.of(done));
        service.markFailed(1L, "expired");
        assertEquals(PaymentStatus.SUCCESS, done.getStatus());
        verifyNoInteractions(events);
    }
}
