package tn.esprit.classe.service;

import tn.esprit.classe.dto.EligibilityResponse;
import tn.esprit.classe.dto.EnrollmentRequest;
import tn.esprit.classe.dto.EnrollmentResponse;

import java.util.List;

public interface EnrollmentService {

    EnrollmentResponse enroll(Long classroomId, EnrollmentRequest request);

    List<EnrollmentResponse> findByClassroom(Long classroomId);

    List<EnrollmentResponse> findByStudent(Long studentId);

    void cancel(Long enrollmentId);

    EligibilityResponse eligibility(Long classroomId, Long studentId);
}
