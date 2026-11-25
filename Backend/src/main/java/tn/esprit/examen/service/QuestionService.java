package tn.esprit.examen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.examen.dto.QuestionRequest;
import tn.esprit.examen.exception.BusinessException;
import tn.esprit.examen.exception.ResourceNotFoundException;
import tn.esprit.examen.model.Examen;
import tn.esprit.examen.model.Question;
import tn.esprit.examen.model.StatutExamen;
import tn.esprit.examen.repository.ExamenRepository;
import tn.esprit.examen.repository.QuestionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ExamenRepository examenRepository;

    public Question ajouterQuestion(String examenId, QuestionRequest r) {
        Examen examen = getExamenModifiable(examenId);
        Question q = new Question();
        copy(r, q);
        q.setExamenId(examenId);
        Question saved = questionRepository.save(q);
        recalculerNote(examen);
        return saved;
    }

    public List<Question> findByExamen(String examenId) {
        if (!examenRepository.existsById(examenId)) {
            throw new ResourceNotFoundException("Examen introuvable : " + examenId);
        }
        return questionRepository.findByExamenId(examenId);
    }

    // modifierQuestion()
    public Question modifierQuestion(String id, QuestionRequest r) {
        Question q = findById(id);
        Examen examen = getExamenModifiable(q.getExamenId());
        copy(r, q);
        Question saved = questionRepository.save(q);
        recalculerNote(examen);
        return saved;
    }

    // supprimerQuestion()
    public void supprimerQuestion(String id) {
        Question q = findById(id);
        Examen examen = getExamenModifiable(q.getExamenId());
        questionRepository.deleteById(id);
        recalculerNote(examen);
    }

    public Question findById(String id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question introuvable : " + id));
    }

    private Examen getExamenModifiable(String examenId) {
        Examen e = examenRepository.findById(examenId)
                .orElseThrow(() -> new ResourceNotFoundException("Examen introuvable : " + examenId));
        if (e.getStatut() == StatutExamen.PUBLIE) {
            throw new BusinessException("Examen publié : questions non modifiables");
        }
        return e;
    }

    private void recalculerNote(Examen e) {
        double total = questionRepository.findByExamenId(e.getId())
                .stream().mapToDouble(Question::getPoints).sum();
        e.setNoteTotale(total);
        examenRepository.save(e);
    }

    private void copy(QuestionRequest r, Question q) {
        q.setEnonce(r.getEnonce());
        q.setTypeQuestion(r.getTypeQuestion());
        q.setOptionA(r.getOptionA());
        q.setOptionB(r.getOptionB());
        q.setOptionC(r.getOptionC());
        q.setOptionD(r.getOptionD());
        q.setBonneReponse(r.getBonneReponse());
        q.setPoints(r.getPoints());
    }
}