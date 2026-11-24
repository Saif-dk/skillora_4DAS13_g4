package tn.esprit.classe.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.classe.dto.PaymentData;
import tn.esprit.classe.dto.PaymentSucceededEvent;
import tn.esprit.classe.model.Enrollment;
import tn.esprit.classe.model.EnrollmentStatus;
import tn.esprit.classe.model.ProcessedEvent;
import tn.esprit.classe.repository.EnrollmentRepository;
import tn.esprit.classe.repository.ProcessedEventRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentEventService {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final ProcessedEventRepository processedEventRepository;

    public void process(PaymentSucceededEvent event) {
        if (event == null || event.getEventId() == null || event.getEventId().isBlank()) {
            log.warn("Événement PaymentSucceeded ignoré : eventId manquant");
            return;
        }
        if (processedEventRepository.existsByEventId(event.getEventId())) {
            log.info("Événement {} déjà traité, ignoré", event.getEventId());
            return;
        }

        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(event.getEventId());
        processedEvent.setProcessedAt(LocalDateTime.now());
        processedEventRepository.save(processedEvent);

        PaymentData data = event.getData();
        if (data == null || data.getEnrollmentId() == null) {
            log.warn("Événement {} sans enrollmentId, ignoré", event.getEventId());
            return;
        }

        Optional<Enrollment> enrollment = enrollmentRepository.findById(data.getEnrollmentId());
        if (enrollment.isEmpty()) {
            log.warn("Inscription {} introuvable pour l'événement {}", data.getEnrollmentId(), event.getEventId());
            return;
        }

        Enrollment entity = enrollment.get();
        if (entity.getStatus() == EnrollmentStatus.PENDING_PAYMENT) {
            entity.setStatus(EnrollmentStatus.ACTIVE);
            enrollmentRepository.save(entity);
            log.info("Inscription {} activée par l'événement {}", entity.getId(), event.getEventId());
        } else {
            log.info("Inscription {} déjà au statut {}, événement {} ignoré",
                    entity.getId(), entity.getStatus(), event.getEventId());
        }
    }
}
