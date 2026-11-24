package tn.esprit.classe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.classe.dto.ClassroomRequest;
import tn.esprit.classe.dto.ClassroomResponse;
import tn.esprit.classe.service.ClassroomService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @PostMapping
    ResponseEntity<ClassroomResponse> create(@Valid @RequestBody ClassroomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classroomService.create(request));
    }

    @GetMapping
    List<ClassroomResponse> list(@RequestParam(required = false) Long courseId,
                                 @RequestParam(required = false) Long instructorId) {
        return classroomService.findAll(courseId, instructorId);
    }

    @GetMapping("/{id}")
    ClassroomResponse detail(@PathVariable Long id) {
        return classroomService.findById(id);
    }

    @PutMapping("/{id}")
    ClassroomResponse update(@PathVariable Long id, @Valid @RequestBody ClassroomRequest request) {
        return classroomService.update(id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        classroomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
