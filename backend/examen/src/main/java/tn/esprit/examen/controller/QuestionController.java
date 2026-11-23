package tn.esprit.examen.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examen.dto.QuestionRequest;
import tn.esprit.examen.model.Question;
import tn.esprit.examen.service.QuestionService;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/{id}")
    public Question detail(@PathVariable String id) {
        return questionService.findById(id);
    }

    @PutMapping("/{id}")
    public Question modifier(@PathVariable String id, @Valid @RequestBody QuestionRequest r) {
        return questionService.modifierQuestion(id, r);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable String id) {
        questionService.supprimerQuestion(id);
        return ResponseEntity.noContent().build();
    }
}