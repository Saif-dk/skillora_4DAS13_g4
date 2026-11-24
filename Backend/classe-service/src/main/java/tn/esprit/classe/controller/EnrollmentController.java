package tn.esprit.classe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.classe.dto.EnrollmentRequest;
import tn.esprit.classe.dto.EnrollmentResponse;
import tn.esprit.classe.service.EnrollmentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/classes/{classId}/enrollments")
    ResponseEntity<EnrollmentResponse> enroll(@PathVariable Long classId,
                                              @Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enroll(classId, request));
    }

    @GetMapping("/classes/{classId}/enrollments")
    List<EnrollmentResponse> byClass(@PathVariable Long classId) {
        return enrollmentService.findByClassroom(classId);
    }

    @GetMapping("/enrollments")
    List<EnrollmentResponse> byStudent(@RequestParam Long studentId) {
        return enrollmentService.findByStudent(studentId);
    }

    @DeleteMapping("/enrollments/{id}")
    ResponseEntity<Void> cancel(@PathVariable Long id) {
        enrollmentService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
