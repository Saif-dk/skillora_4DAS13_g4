package com.skillora.course.api.controller.internal;

import com.skillora.course.api.dto.CourseResponse;
import com.skillora.course.security.AuthenticatedUser;
import com.skillora.course.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal")
public class CourseInternalController {

    private final CourseService courseService;

    public CourseInternalController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<CourseResponse> getCourseById(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(courseService.getCourseById(id, user.userId(), user.role()));
    }
}
