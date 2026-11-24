package tn.esprit.classe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.classe.model.ProcessedEvent;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {

    boolean existsByEventId(String eventId);
}
