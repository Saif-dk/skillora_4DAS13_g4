package tn.esprit.classe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.classe.dto.SessionMapper;
import tn.esprit.classe.dto.SessionRequest;
import tn.esprit.classe.dto.SessionResponse;
import tn.esprit.classe.dto.SessionStatusRequest;
import tn.esprit.classe.exception.BusinessException;
import tn.esprit.classe.exception.ResourceNotFoundException;
import tn.esprit.classe.model.ClassSession;
import tn.esprit.classe.model.Classroom;
import tn.esprit.classe.repository.ClassSessionRepository;
import tn.esprit.classe.repository.ClassroomRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ClassSessionServiceImpl implements ClassSessionService {

    private final ClassSessionRepository classSessionRepository;
    private final ClassroomRepository classroomRepository;

    @Override
    public SessionResponse create(Long classroomId, SessionRequest request) {
        validateDates(request.getStartDate(), request.getEndDate());
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe introuvable"));
        ClassSession session = new ClassSession();
        applyRequest(session, request);
        classroom.addSession(session);
        return SessionMapper.toResponse(classSessionRepository.save(session));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> findByClassroom(Long classroomId) {
        if (!classroomRepository.existsById(classroomId)) {
            throw new ResourceNotFoundException("Classe introuvable");
        }
        return classSessionRepository.findByClassroomIdOrderByStartDateAsc(classroomId).stream()
                .map(SessionMapper::toResponse)
                .toList();
    }

    @Override
    public SessionResponse update(Long sessionId, SessionRequest request) {
        validateDates(request.getStartDate(), request.getEndDate());
        ClassSession session = findEntityById(sessionId);
        applyRequest(session, request);
        return SessionMapper.toResponse(classSessionRepository.save(session));
    }

    @Override
    public SessionResponse updateStatus(Long sessionId, SessionStatusRequest request) {
        ClassSession session = findEntityById(sessionId);
        session.setStatus(request.getStatus());
        return SessionMapper.toResponse(classSessionRepository.save(session));
    }

    @Override
    public void delete(Long sessionId) {
        classSessionRepository.delete(findEntityById(sessionId));
    }

    private ClassSession findEntityById(Long sessionId) {
        return classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance introuvable"));
    }

    private void applyRequest(ClassSession session, SessionRequest request) {
        session.setTitle(request.getTitle());
        session.setStartDate(request.getStartDate());
        session.setEndDate(request.getEndDate());
        session.setMeetingLink(request.getMeetingLink());
    }

    private void validateDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("Les dates de début et de fin sont obligatoires");
        }
        if (!endDate.isAfter(startDate)) {
            throw new BusinessException("La date de fin doit être postérieure à la date de début");
        }
    }
}
