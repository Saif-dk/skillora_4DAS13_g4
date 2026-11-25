package tn.esprit.examen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.dto.ExamenRequest;
import tn.esprit.examen.dto.QuestionRequest;
import tn.esprit.examen.model.Examen;
import tn.esprit.examen.model.Question;
import tn.esprit.examen.service.ExamenService;
import tn.esprit.examen.service.QuestionService;

import java.util.List;

@RestController
@RequestMapping("/api/examens")
@RequiredArgsConstructor
public class ExamenController {

    private final ExamenService examenService;
    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<Examen> creer(@Valid @RequestBody ExamenRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(examenService.creerExamen(r));
    }

    @GetMapping
    public List<Examen> liste(@RequestParam(required = false) String courseId) {
        return courseId == null ? examenService.findAll() : examenService.findByCourse(courseId);
    }

    @GetMapping("/{id}")
    public Examen detail(@PathVariable String id) {
        return examenService.findById(id);
    }

    @PutMapping("/{id}")
    public Examen modifier(@PathVariable String id, @Valid @RequestBody ExamenRequest r) {
        return examenService.modifierExamen(id, r);
    }

    @PatchMapping("/{id}/publier")
    public Examen publier(@PathVariable String id) {
        return examenService.publierExamen(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable String id) {
        examenService.supprimerExamen(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Questions d'un examen ----
    @PostMapping("/{id}/questions")
    public ResponseEntity<Question> ajouterQuestion(@PathVariable String id,
                                                    @Valid @RequestBody QuestionRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.ajouterQuestion(id, r));
    }

    @GetMapping("/{id}/questions")
    public List<Question> questions(@PathVariable String id) {
        return questionService.findByExamen(id);
    }
}