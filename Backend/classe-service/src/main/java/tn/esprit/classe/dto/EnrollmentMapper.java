package tn.esprit.classe.dto;

import tn.esprit.classe.model.Enrollment;

public final class EnrollmentMapper {

    private EnrollmentMapper() {
    }

    public static EnrollmentResponse toResponse(Enrollment enrollment) {
        Long classroomId = enrollment.getClassroom() == null ? null : enrollment.getClassroom().getId();
        return new EnrollmentResponse(
                enrollment.getId(),
                classroomId,
                enrollment.getStudentId(),
                enrollment.getEnrolledAt(),
                enrollment.getStatus().name());
    }
}
