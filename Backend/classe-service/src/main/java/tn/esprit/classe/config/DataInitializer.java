package tn.esprit.classe.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.classe.model.ClassSession;
import tn.esprit.classe.model.Classroom;
import tn.esprit.classe.model.ClassroomStatus;
import tn.esprit.classe.model.Enrollment;
import tn.esprit.classe.model.EnrollmentStatus;
import tn.esprit.classe.model.SessionStatus;
import tn.esprit.classe.repository.ClassroomRepository;
import tn.esprit.classe.repository.EnrollmentRepository;

import java.time.LocalDateTime;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (classroomRepository.count() > 0) {
            return;
        }

        Classroom first = classroom("Java Spring Boot", "Initiation aux microservices", 1L, 10L, 30,
                ClassroomStatus.ACTIVE);
        first.addSession(session("Introduction à Spring Boot", LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(2), "https://meet.example.com/java-1"));
        classroomRepository.save(first);

        Classroom second = classroom("Angular Avancé", "Composants et services", 2L, 20L, 25,
                ClassroomStatus.ACTIVE);
        second.addSession(session("RxJS et observables", LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(3).plusHours(2), "https://meet.example.com/angular-1"));
        classroomRepository.save(second);

        enrollmentRepository.save(enrollment(first, 101L, EnrollmentStatus.PENDING_PAYMENT));
        enrollmentRepository.save(enrollment(second, 102L, EnrollmentStatus.ACTIVE));
    }

    private Classroom classroom(String name, String description, Long courseId, Long instructorId,
                                Integer capacity, ClassroomStatus status) {
        Classroom classroom = new Classroom();
        classroom.setName(name);
        classroom.setDescription(description);
        classroom.setCourseId(courseId);
        classroom.setInstructorId(instructorId);
        classroom.setCapacity(capacity);
        classroom.setStatus(status);
        return classroom;
    }

    private ClassSession session(String title, LocalDateTime startDate, LocalDateTime endDate, String meetingLink) {
        ClassSession session = new ClassSession();
        session.setTitle(title);
        session.setStartDate(startDate);
        session.setEndDate(endDate);
        session.setMeetingLink(meetingLink);
        session.setStatus(SessionStatus.PLANNED);
        return session;
    }

    private Enrollment enrollment(Classroom classroom, Long studentId, EnrollmentStatus status) {
        Enrollment enrollment = new Enrollment();
        enrollment.setClassroom(classroom);
        enrollment.setStudentId(studentId);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStatus(status);
        return enrollment;
    }
}
