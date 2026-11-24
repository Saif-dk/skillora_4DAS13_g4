package tn.esprit.classe.dto;

import tn.esprit.classe.model.ClassSession;

public final class SessionMapper {

    private SessionMapper() {
    }

    public static SessionResponse toResponse(ClassSession session) {
        Long classroomId = session.getClassroom() == null ? null : session.getClassroom().getId();
        return new SessionResponse(
                session.getId(),
                classroomId,
                session.getTitle(),
                session.getStartDate(),
                session.getEndDate(),
                session.getMeetingLink(),
                session.getStatus().name());
    }
}
