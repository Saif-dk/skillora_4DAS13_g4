package com.skillora.course.api.controller;

import com.skillora.course.api.dto.CourseCreateRequest;
import com.skillora.course.api.dto.CourseResponse;
import com.skillora.course.api.dto.CourseSummaryResponse;
import com.skillora.course.api.dto.CourseUpdateRequest;
import com.skillora.course.api.dto.PageResponse;
import com.skillora.course.domain.enumeration.CourseStatus;
import com.skillora.course.security.AuthenticatedUser;
import com.skillora.course.service.CourseFilter;
import com.skillora.course.service.CourseService;
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

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CourseCreateRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = currentUser(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.createCourse(request, user.userId()));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CourseSummaryResponse>> listCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            Authentication authentication
    ) {
        AuthenticatedUser user = currentUser(authentication);
        CourseFilter filter = CourseFilter.of(search, status, page, size);
        return ResponseEntity.ok(courseService.listCourses(filter, user.userId(), user.role()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        AuthenticatedUser user = currentUser(authentication);
        return ResponseEntity.ok(courseService.getCourseById(id, user.userId(), user.role()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable("id") Long id,
            @Valid @RequestBody CourseUpdateRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = currentUser(authentication);
        return ResponseEntity.ok(courseService.updateCourse(id, request, user.userId(), user.role()));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<CourseResponse> publishCourse(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        AuthenticatedUser user = currentUser(authentication);
        return ResponseEntity.ok(courseService.publishCourse(id, user.userId(), user.role()));
    }

    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<CourseResponse> unpublishCourse(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        AuthenticatedUser user = currentUser(authentication);
        return ResponseEntity.ok(courseService.unpublishCourse(id, user.userId(), user.role()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        AuthenticatedUser user = currentUser(authentication);
        courseService.deleteCourse(id, user.userId(), user.role());
        return ResponseEntity.noContent().build();
    }

    private AuthenticatedUser currentUser(Authentication authentication) {
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
