package tn.esprit.classe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.classe.model.Classroom;

import java.util.List;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    List<Classroom> findByCourseId(Long courseId);

    List<Classroom> findByInstructorId(Long instructorId);

    List<Classroom> findByCourseIdAndInstructorId(Long courseId, Long instructorId);
}
