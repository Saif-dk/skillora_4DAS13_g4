package tn.esprit.examen.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.esprit.examen.model.Examen;

import java.util.List;

public interface ExamenRepository extends MongoRepository<Examen, String> {
    List<Examen> findByCourseId(String courseId);
    List<Examen> findByTeacherId(String teacherId);
}