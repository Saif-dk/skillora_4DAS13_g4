package tn.esprit.examen.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.esprit.examen.model.Question;

import java.util.List;

public interface QuestionRepository extends MongoRepository<Question, String> {
    List<Question> findByExamenId(String examenId);
    void deleteByExamenId(String examenId);
}