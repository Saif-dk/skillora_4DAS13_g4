package com.skillora.course.api.controller;

import com.skillora.course.api.dto.LessonCreateRequest;
import com.skillora.course.api.dto.LessonResponse;
import com.skillora.course.api.dto.LessonUpdateRequest;
import com.skillora.course.security.AuthenticatedUser;
import com.skillora.course.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<List<LessonResponse>> listLessons(
            @PathVariable("courseId") Long courseId,
            Authentication authentication
    ) {
        AuthenticatedUser user = currentUser(authentication);
        return ResponseEntity.ok(lessonService.listLessons(courseId, user.userId(), user.role()));
    }

    @PostMapping("/courses/{courseId}/lessons")
    public ResponseEntity<LessonResponse> addLesson(
            @PathVariable("courseId") Long courseId,
            @Valid @RequestBody LessonCreateRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = currentUser(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lessonService.addLesson(courseId, request, user.userId(), user.role()));
    }

    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable("lessonId") Long lessonId,
            @Valid @RequestBody LessonUpdateRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = currentUser(authentication);
        return ResponseEntity.ok(lessonService.updateLesson(lessonId, request, user.userId(), user.role()));
    }

    @PatchMapping("/lessons/{lessonId}/reorder")
    public ResponseEntity<LessonResponse> reorderLesson(
            @PathVariable("lessonId") Long lessonId,
            @RequestParam("position") Integer position,
            Authentication authentication
    ) {
        AuthenticatedUser user = currentUser(authentication);
        return ResponseEntity.ok(lessonService.reorderLesson(lessonId, position, user.userId(), user.role()));
    }

    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable("lessonId") Long lessonId,
            Authentication authentication
    ) {
        AuthenticatedUser user = currentUser(authentication);
        lessonService.deleteLesson(lessonId, user.userId(), user.role());
        return ResponseEntity.noContent().build();
    }

    private AuthenticatedUser currentUser(Authentication authentication) {
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
