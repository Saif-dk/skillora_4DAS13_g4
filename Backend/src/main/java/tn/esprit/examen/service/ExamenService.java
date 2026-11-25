package tn.esprit.examen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.examen.dto.ExamenRequest;
import tn.esprit.examen.exception.BusinessException;
import tn.esprit.examen.exception.ResourceNotFoundException;
import tn.esprit.examen.model.Examen;
import tn.esprit.examen.model.StatutExamen;
import tn.esprit.examen.repository.ExamenRepository;
import tn.esprit.examen.repository.QuestionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamenService {

    private final ExamenRepository examenRepository;
    private final QuestionRepository questionRepository;

    // creerExamen()
    public Examen creerExamen(ExamenRequest r) {
        Examen e = Examen.builder()
                .titre(r.getTitre())
                .description(r.getDescription())
                .dateExamen(r.getDateExamen())
                .duree(r.getDuree())
                .courseId(r.getCourseId())
                .teacherId(r.getTeacherId())
                .noteTotale(0)
                .statut(StatutExamen.BROUILLON)
                .build();
        // TODO (communication synchrone) : vérifier que courseId existe via le microservice Cours
        return examenRepository.save(e);
    }

    public List<Examen> findAll() { return examenRepository.findAll(); }

    public List<Examen> findByCourse(String courseId) { return examenRepository.findByCourseId(courseId); }

    public Examen findById(String id) {
        return examenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examen introuvable : " + id));
    }

    // modifierExamen()
    public Examen modifierExamen(String id, ExamenRequest r) {
        Examen e = findById(id);
        if (e.getStatut() == StatutExamen.PUBLIE) {
            throw new BusinessException("Un examen publié ne peut plus être modifié");
        }
        e.setTitre(r.getTitre());
        e.setDescription(r.getDescription());
        e.setDateExamen(r.getDateExamen());
        e.setDuree(r.getDuree());
        e.setCourseId(r.getCourseId());
        e.setTeacherId(r.getTeacherId());
        return examenRepository.save(e);
    }

    // publierExamen()
    public Examen publierExamen(String id) {
        Examen e = findById(id);
        if (e.getStatut() == StatutExamen.PUBLIE) {
            throw new BusinessException("Examen déjà publié");
        }
        if (questionRepository.findByExamenId(id).isEmpty()) {
            throw new BusinessException("Impossible de publier un examen sans question (1..*)");
        }
        e.setStatut(StatutExamen.PUBLIE);
        Examen saved = examenRepository.save(e);
        // TODO (communication asynchrone) : publier l'événement ExamenPublie (RabbitMQ/Kafka)
        return saved;
    }

    // supprimerExamen() : composition => supprime aussi les questions
    public void supprimerExamen(String id) {
        findById(id);
        questionRepository.deleteByExamenId(id);
        examenRepository.deleteById(id);
    }
}