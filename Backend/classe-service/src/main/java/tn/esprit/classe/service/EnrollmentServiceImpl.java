package tn.esprit.classe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.classe.dto.EligibilityResponse;
import tn.esprit.classe.dto.EnrollmentMapper;
import tn.esprit.classe.dto.EnrollmentRequest;
import tn.esprit.classe.dto.EnrollmentResponse;
import tn.esprit.classe.exception.BusinessException;
import tn.esprit.classe.exception.ConflictException;
import tn.esprit.classe.exception.ResourceNotFoundException;
import tn.esprit.classe.model.Classroom;
import tn.esprit.classe.model.ClassroomStatus;
import tn.esprit.classe.model.Enrollment;
import tn.esprit.classe.model.EnrollmentStatus;
import tn.esprit.classe.repository.ClassroomRepository;
import tn.esprit.classe.repository.EnrollmentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final ClassroomRepository classroomRepository;

    @Override
    public EnrollmentResponse enroll(Long classroomId, EnrollmentRequest request) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe introuvable"));
        if (classroom.getStatus() != ClassroomStatus.ACTIVE) {
            throw new BusinessException("La classe n'est pas active");
        }

        Optional<Enrollment> existing = enrollmentRepository
                .findByClassroomIdAndStudentId(classroomId, request.getStudentId());
        if (existing.isPresent() && existing.get().getStatus() != EnrollmentStatus.CANCELLED) {
            throw new ConflictException("L'étudiant est déjà inscrit dans cette classe");
        }

        long activeEnrollments = enrollmentRepository
                .countByClassroomIdAndStatusNot(classroomId, EnrollmentStatus.CANCELLED);
        if (activeEnrollments >= classroom.getCapacity()) {
            throw new ConflictException("La capacité de la classe est atteinte");
        }

        Enrollment enrollment;
        if (existing.isPresent()) {
            enrollment = existing.get();
            enrollment.setStatus(EnrollmentStatus.PENDING_PAYMENT);
            enrollment.setEnrolledAt(LocalDateTime.now());
        } else {
            enrollment = new Enrollment();
            enrollment.setClassroom(classroom);
            enrollment.setStudentId(request.getStudentId());
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setStatus(EnrollmentStatus.PENDING_PAYMENT);
        }
        return EnrollmentMapper.toResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByClassroom(Long classroomId) {
        if (!classroomRepository.existsById(classroomId)) {
            throw new ResourceNotFoundException("Classe introuvable");
        }
        return enrollmentRepository.findByClassroomId(classroomId).stream()
                .map(EnrollmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(EnrollmentMapper::toResponse)
                .toList();
    }

    @Override
    public void cancel(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription introuvable"));
        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public EligibilityResponse eligibility(Long classroomId, Long studentId) {
        if (!classroomRepository.existsById(classroomId)) {
            throw new ResourceNotFoundException("Classe introuvable");
        }
        Optional<Enrollment> enrollment = enrollmentRepository
                .findByClassroomIdAndStudentId(classroomId, studentId);
        boolean eligible = enrollment.isPresent()
                && enrollment.get().getStatus() == EnrollmentStatus.ACTIVE;
        String status = enrollment.map(value -> value.getStatus().name()).orElse(null);
        return new EligibilityResponse(eligible, status);
    }
}
