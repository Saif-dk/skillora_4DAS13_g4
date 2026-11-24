package tn.esprit.classe.dto;

import tn.esprit.classe.model.Classroom;
import tn.esprit.classe.model.ClassSession;

import java.util.List;

public final class ClassroomMapper {

    private ClassroomMapper() {
    }

    public static ClassroomResponse toResponse(Classroom classroom) {
        List<SessionResponse> sessions = classroom.getSessions().stream()
                .map(SessionMapper::toResponse)
                .toList();
        return new ClassroomResponse(
                classroom.getId(),
                classroom.getName(),
                classroom.getDescription(),
                classroom.getCourseId(),
                classroom.getInstructorId(),
                classroom.getCapacity(),
                classroom.getStatus().name(),
                sessions);
    }
}
