package tn.esprit.classe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.classe.model.Enrollment;
import tn.esprit.classe.model.EnrollmentStatus;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByClassroomId(Long classroomId);

    List<Enrollment> findByStudentId(Long studentId);

    Optional<Enrollment> findByClassroomIdAndStudentId(Long classroomId, Long studentId);

    long countByClassroomIdAndStatusNot(Long classroomId, EnrollmentStatus status);
}
