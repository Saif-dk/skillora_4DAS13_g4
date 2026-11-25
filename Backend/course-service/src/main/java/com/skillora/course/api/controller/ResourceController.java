package com.skillora.course.api.controller;

import com.skillora.course.api.dto.ResourceCreateRequest;
import com.skillora.course.api.dto.ResourceResponse;
import com.skillora.course.domain.enumeration.ResourceType;
import com.skillora.course.security.AuthenticatedUser;
import com.skillora.course.service.FileStorageService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1")
public class ResourceController {

    private final ResourceService resourceService;
    private final FileStorageService fileStorageService;

    public ResourceController(ResourceService resourceService, FileStorageService fileStorageService) {
        this.resourceService = resourceService;
        this.fileStorageService = fileStorageService;
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

    @PostMapping("/courses/{courseId}/resources/upload")
    public ResponseEntity<ResourceResponse> uploadCourseResource(
            @PathVariable("courseId") Long courseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "type", required = false) ResourceType type,
            @RequestParam(value = "size", required = false) String size,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saveUpload(courseId, null, file, name, type, size, authentication));
    }

    @PostMapping("/lessons/{lessonId}/resources/upload")
    public ResponseEntity<ResourceResponse> uploadLessonResource(
            @PathVariable("lessonId") Long lessonId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "type", required = false) ResourceType type,
            @RequestParam(value = "size", required = false) String size,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saveUpload(null, lessonId, file, name, type, size, authentication));
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

    private ResourceResponse saveUpload(
            Long courseId,
            Long lessonId,
            MultipartFile file,
            String name,
            ResourceType type,
            String size,
            Authentication authentication
    ) {
        AuthenticatedUser user = currentUser(authentication);
        FileStorageService.StoredFile stored = fileStorageService.store(file);
        String resourceName = name == null || name.isBlank() ? stored.originalName() : name;
        ResourceType resourceType = type == null ? ResourceType.DOCUMENT : type;
        String sizeLabel = size == null || size.isBlank() ? stored.sizeLabel() : size;
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(stored.storedName())
                .toUriString();
        ResourceCreateRequest request = new ResourceCreateRequest(
                courseId, lessonId, resourceName, resourceType, url, sizeLabel);
        return resourceService.createResource(request, user.userId(), user.role());
    }

    private AuthenticatedUser currentUser(Authentication authentication) {
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
