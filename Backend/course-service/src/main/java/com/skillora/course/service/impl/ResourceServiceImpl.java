package com.skillora.course.service.impl;

import com.skillora.course.api.dto.ResourceCreateRequest;
import com.skillora.course.api.dto.ResourceResponse;
import com.skillora.course.api.exception.BusinessConflictException;
import com.skillora.course.api.exception.ResourceNotFoundException;
import com.skillora.course.domain.entity.Course;
import com.skillora.course.domain.entity.Lesson;
import com.skillora.course.domain.entity.Resource;
import com.skillora.course.mapper.ResourceMapper;
import com.skillora.course.repository.CourseRepository;
import com.skillora.course.repository.LessonRepository;
import com.skillora.course.repository.ResourceRepository;
import com.skillora.course.service.CourseAccessPolicy;
import com.skillora.course.service.ResourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final CourseAccessPolicy accessPolicy;

    public ResourceServiceImpl(ResourceRepository resourceRepository,
                               CourseRepository courseRepository,
                               LessonRepository lessonRepository,
                               CourseAccessPolicy accessPolicy) {
        this.resourceRepository = resourceRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.accessPolicy = accessPolicy;
    }

    @Override
    @Transactional
    public ResourceResponse createResource(ResourceCreateRequest request, Long userId, String role) {
        boolean hasCourse = request.courseId() != null;
        boolean hasLesson = request.lessonId() != null;
        if (hasCourse == hasLesson) {
            throw new BusinessConflictException(
                    "La ressource doit etre attachee a exactement un cours ou une lecon");
        }

        Resource resource = ResourceMapper.toEntity(request);
        Course course;
        if (hasCourse) {
            course = findCourse(request.courseId());
            resource.setCourse(course);
        } else {
            Lesson lesson = findLesson(request.lessonId());
            course = requireCourse(lesson);
            resource.setLesson(lesson);
        }
        accessPolicy.requireCanModify(course, userId, role);
        return ResourceMapper.toResponse(resourceRepository.save(resource));
    }

    @Override
    public ResourceResponse getResourceById(Long resourceId, Long userId, String role) {
        Resource resource = findResource(resourceId);
        accessPolicy.requireCanView(requireCourse(resource), userId, role);
        return ResourceMapper.toResponse(resource);
    }

    @Override
    @Transactional
    public void deleteResource(Long resourceId, Long userId, String role) {
        Resource resource = findResource(resourceId);
        accessPolicy.requireCanModify(requireCourse(resource), userId, role);
        resourceRepository.delete(resource);
    }

    private Course requireCourse(Resource resource) {
        if (resource.getCourse() != null) {
            return resource.getCourse();
        }
        if (resource.getLesson() != null) {
            return requireCourse(resource.getLesson());
        }
        throw new ResourceNotFoundException("Cours introuvable pour cette ressource");
    }

    private Course requireCourse(Lesson lesson) {
        if (lesson.getCourse() == null) {
            throw new ResourceNotFoundException("Cours introuvable pour cette lecon");
        }
        return lesson.getCourse();
    }

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours introuvable"));
    }

    private Lesson findLesson(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecon introuvable"));
    }

    private Resource findResource(Long resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Ressource introuvable"));
    }
}
