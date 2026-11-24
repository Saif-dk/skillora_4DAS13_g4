package tn.esprit.classe.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.classe.dto.EligibilityResponse;
import tn.esprit.classe.service.EnrollmentService;

@RestController
@RequestMapping("/api/v1/internal/enrollments")
@RequiredArgsConstructor
public class InternalController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/eligibility")
    EligibilityResponse eligibility(@RequestParam Long classId, @RequestParam Long studentId) {
        return enrollmentService.eligibility(classId, studentId);
    }
}
