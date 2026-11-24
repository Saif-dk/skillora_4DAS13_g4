package tn.esprit.classe.service;

import tn.esprit.classe.dto.SessionRequest;
import tn.esprit.classe.dto.SessionResponse;
import tn.esprit.classe.dto.SessionStatusRequest;

import java.util.List;

public interface ClassSessionService {

    SessionResponse create(Long classroomId, SessionRequest request);

    List<SessionResponse> findByClassroom(Long classroomId);

    SessionResponse update(Long sessionId, SessionRequest request);

    SessionResponse updateStatus(Long sessionId, SessionStatusRequest request);

    void delete(Long sessionId);
}
