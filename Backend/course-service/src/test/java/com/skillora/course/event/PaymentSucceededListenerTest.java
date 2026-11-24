package com.skillora.course.event;

import com.skillora.course.domain.entity.ProcessedEvent;
import com.skillora.course.repository.ProcessedEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentSucceededListenerTest {

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Test
    void handlePaymentSucceededSavesProcessedEvent() {
        PaymentSucceededListener listener = new PaymentSucceededListener(processedEventRepository);
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "evt-pay-1", 100L, 200L, 300L, 400L, new BigDecimal("49.90"), "EUR",
                LocalDateTime.of(2026, 11, 24, 10, 0));
        when(processedEventRepository.existsByEventId("evt-pay-1")).thenReturn(false);

        listener.handlePaymentSucceeded(event);

        ArgumentCaptor<ProcessedEvent> captor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository).save(captor.capture());
        ProcessedEvent saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo("evt-pay-1");
        assertThat(saved.getEventType()).isEqualTo("PaymentSucceeded");
        assertThat(saved.getProcessedAt()).isNotNull();
    }

    @Test
    void handlePaymentSucceededIgnoresDuplicateEvent() {
        PaymentSucceededListener listener = new PaymentSucceededListener(processedEventRepository);
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                "evt-pay-dup", 100L, 200L, 300L, 400L, new BigDecimal("49.90"), "EUR",
                LocalDateTime.of(2026, 11, 24, 10, 0));
        when(processedEventRepository.existsByEventId("evt-pay-dup")).thenReturn(true);

        listener.handlePaymentSucceeded(event);

        verify(processedEventRepository, never()).save(any(ProcessedEvent.class));
    }
}
