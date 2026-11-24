package com.skillora.course.event;

import com.skillora.course.config.RabbitMQConfig;
import com.skillora.course.domain.entity.ProcessedEvent;
import com.skillora.course.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaymentSucceededListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentSucceededListener.class);

    private final ProcessedEventRepository processedEventRepository;

    public PaymentSucceededListener(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.COURSE_PAYMENT_SUCCEEDED_QUEUE)
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        if (event == null || event.eventId() == null) {
            log.warn("Ignoring PaymentSucceeded event with missing eventId");
            return;
        }

        if (processedEventRepository.existsByEventId(event.eventId())) {
            log.info("Ignoring duplicate PaymentSucceeded eventId={}", event.eventId());
            return;
        }

        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(event.eventId());
        processedEvent.setEventType("PaymentSucceeded");
        processedEvent.setProcessedAt(LocalDateTime.now());
        processedEventRepository.save(processedEvent);

        log.info("Processed PaymentSucceeded eventId={} paymentId={} enrollmentId={} courseId={}",
                event.eventId(), event.paymentId(), event.enrollmentId(), event.courseId());
    }
}
