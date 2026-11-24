package tn.esprit.classe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.classe.dto.SessionRequest;
import tn.esprit.classe.dto.SessionResponse;
import tn.esprit.classe.dto.SessionStatusRequest;
import tn.esprit.classe.service.ClassSessionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SessionController {

    private final ClassSessionService classSessionService;

    @PostMapping("/classes/{classId}/sessions")
    ResponseEntity<SessionResponse> create(@PathVariable Long classId,
                                           @Valid @RequestBody SessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classSessionService.create(classId, request));
    }

    @GetMapping("/classes/{classId}/sessions")
    List<SessionResponse> list(@PathVariable Long classId) {
        return classSessionService.findByClassroom(classId);
    }

    @PutMapping("/sessions/{id}")
    SessionResponse update(@PathVariable Long id, @Valid @RequestBody SessionRequest request) {
        return classSessionService.update(id, request);
    }

    @PatchMapping("/sessions/{id}/status")
    SessionResponse updateStatus(@PathVariable Long id, @Valid @RequestBody SessionStatusRequest request) {
        return classSessionService.updateStatus(id, request);
    }

    @DeleteMapping("/sessions/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        classSessionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
