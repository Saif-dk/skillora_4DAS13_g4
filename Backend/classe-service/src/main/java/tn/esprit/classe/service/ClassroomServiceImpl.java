package tn.esprit.classe.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.classe.client.CourseClient;
import tn.esprit.classe.dto.ClassroomMapper;
import tn.esprit.classe.dto.ClassroomRequest;
import tn.esprit.classe.dto.ClassroomResponse;
import tn.esprit.classe.exception.ConflictException;
import tn.esprit.classe.exception.ResourceNotFoundException;
import tn.esprit.classe.exception.ServiceUnavailableException;
import tn.esprit.classe.model.Classroom;
import tn.esprit.classe.model.ClassroomStatus;
import tn.esprit.classe.model.Enrollment;
import tn.esprit.classe.model.EnrollmentStatus;
import tn.esprit.classe.repository.ClassroomRepository;
import tn.esprit.classe.repository.EnrollmentRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseClient courseClient;

    @Value("${services.cours.check-enabled:true}")
    private boolean courseCheckEnabled;

    @Override
    public ClassroomResponse create(ClassroomRequest request) {
        checkCourseExists(request.getCourseId());
        Classroom classroom = new Classroom();
        applyRequest(classroom, request);
        classroom.setStatus(ClassroomStatus.ACTIVE);
        return ClassroomMapper.toResponse(classroomRepository.save(classroom));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomResponse> findAll(Long courseId, Long instructorId) {
        List<Classroom> classrooms;
        if (courseId != null && instructorId != null) {
            classrooms = classroomRepository.findByCourseIdAndInstructorId(courseId, instructorId);
        } else if (courseId != null) {
            classrooms = classroomRepository.findByCourseId(courseId);
        } else if (instructorId != null) {
            classrooms = classroomRepository.findByInstructorId(instructorId);
        } else {
            classrooms = classroomRepository.findAll();
        }
        return classrooms.stream().map(ClassroomMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClassroomResponse findById(Long id) {
        return ClassroomMapper.toResponse(findEntityById(id));
    }

    @Override
    public ClassroomResponse update(Long id, ClassroomRequest request) {
        Classroom classroom = findEntityById(id);
        checkCourseExists(request.getCourseId());
        long activeEnrollments = enrollmentRepository.countByClassroomIdAndStatusNot(id, EnrollmentStatus.CANCELLED);
        if (activeEnrollments > request.getCapacity()) {
            throw new ConflictException("La capacité ne peut pas être inférieure au nombre d'inscriptions actives");
        }
        applyRequest(classroom, request);
        return ClassroomMapper.toResponse(classroomRepository.save(classroom));
    }

    @Override
    public void delete(Long id) {
        Classroom classroom = findEntityById(id);
        List<Enrollment> enrollments = enrollmentRepository.findByClassroomId(id);
        boolean hasActiveEnrollment = enrollments.stream()
                .anyMatch(enrollment -> enrollment.getStatus() == EnrollmentStatus.ACTIVE);
        if (hasActiveEnrollment) {
            throw new ConflictException("Impossible de supprimer une classe avec des inscriptions ACTIVE");
        }
        enrollmentRepository.deleteAll(enrollments);
        enrollmentRepository.flush();
        classroomRepository.delete(classroom);
    }

    private Classroom findEntityById(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classe introuvable"));
    }

    private void applyRequest(Classroom classroom, ClassroomRequest request) {
        classroom.setName(request.getName());
        classroom.setDescription(request.getDescription());
        classroom.setCourseId(request.getCourseId());
        classroom.setInstructorId(request.getInstructorId());
        classroom.setCapacity(request.getCapacity());
    }

    private void checkCourseExists(Long courseId) {
        if (!courseCheckEnabled) {
            return;
        }
        try {
            courseClient.getCourseById(courseId);
        } catch (FeignException exception) {
            if (exception.status() == 404) {
                throw new ResourceNotFoundException("Cours introuvable");
            }
            throw new ServiceUnavailableException("Service Cours indisponible");
        } catch (RuntimeException exception) {
            throw new ServiceUnavailableException("Service Cours indisponible");
        }
    }
}
