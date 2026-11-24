package tn.esprit.classe.service;

import tn.esprit.classe.dto.ClassroomRequest;
import tn.esprit.classe.dto.ClassroomResponse;

import java.util.List;

public interface ClassroomService {

    ClassroomResponse create(ClassroomRequest request);

    List<ClassroomResponse> findAll(Long courseId, Long instructorId);

    ClassroomResponse findById(Long id);

    ClassroomResponse update(Long id, ClassroomRequest request);

    void delete(Long id);
}
