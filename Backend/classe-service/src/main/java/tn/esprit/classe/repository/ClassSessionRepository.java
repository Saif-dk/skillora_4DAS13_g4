package tn.esprit.classe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.classe.model.ClassSession;

import java.util.List;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    List<ClassSession> findByClassroomIdOrderByStartDateAsc(Long classroomId);
}
