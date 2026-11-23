package com.skillora.course.api.controller;

import com.skillora.course.api.dto.ResourceCreateRequest;
import com.skillora.course.api.dto.ResourceResponse;
import com.skillora.course.security.AuthenticatedUser;
import com.skillora.course.service.ResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping("/courses/{courseId}/resources")
    public ResponseEntity<ResourceResponse> createCourseResource(
            @PathVariable("courseId") Long courseId,
            @Valid @RequestBody ResourceCreateRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = currentUser(authentication);
        ResourceCreateRequest attachedRequest = new ResourceCreateRequest(
                courseId, null, request.name(), request.type(), request.url(), request.size());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resourceService.createResource(attachedRequest, user.userId(), user.role()));
    }

    @PostMapping("/lessons/{lessonId}/resources")
    public ResponseEntity<ResourceResponse> createLessonResource(
            @PathVariable("lessonId") Long lessonId,
            @Valid @RequestBody ResourceCreateRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = currentUser(authentication);
        ResourceCreateRequest attachedRequest = new ResourceCreateRequest(
                null, lessonId, request.name(), request.type(), request.url(), request.size());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resourceService.createResource(attachedRequest, user.userId(), user.role()));
    }

    @GetMapping("/resources/{id}")
    public ResponseEntity<ResourceResponse> getResourceById(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        AuthenticatedUser user = currentUser(authentication);
        return ResponseEntity.ok(resourceService.getResourceById(id, user.userId(), user.role()));
    }

    @DeleteMapping("/resources/{id}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable("id") Long id,
            Authentication authentication
    ) {
        AuthenticatedUser user = currentUser(authentication);
        resourceService.deleteResource(id, user.userId(), user.role());
        return ResponseEntity.noContent().build();
    }

    private AuthenticatedUser currentUser(Authentication authentication) {
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
