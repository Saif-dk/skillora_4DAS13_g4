package com.skillora.course.service.impl;

import com.skillora.course.api.dto.CourseCreateRequest;
import com.skillora.course.api.dto.CourseResponse;
import com.skillora.course.api.dto.CourseSummaryResponse;
import com.skillora.course.api.dto.CourseUpdateRequest;
import com.skillora.course.api.dto.PageResponse;
import com.skillora.course.api.exception.BusinessConflictException;
import com.skillora.course.api.exception.ForbiddenOperationException;
import com.skillora.course.api.exception.ResourceNotFoundException;
import com.skillora.course.domain.entity.Course;
import com.skillora.course.domain.enumeration.CourseStatus;
import com.skillora.course.event.CourseCreatedEvent;
import com.skillora.course.event.CourseEventPublisher;
import com.skillora.course.mapper.CourseMapper;
import com.skillora.course.repository.CourseRepository;
import com.skillora.course.service.CourseAccessPolicy;
import com.skillora.course.service.CourseFilter;
import com.skillora.course.service.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseAccessPolicy accessPolicy;
    private final CourseEventPublisher courseEventPublisher;

    public CourseServiceImpl(CourseRepository courseRepository, CourseAccessPolicy accessPolicy,
                             CourseEventPublisher courseEventPublisher) {
        this.courseRepository = courseRepository;
        this.accessPolicy = accessPolicy;
        this.courseEventPublisher = courseEventPublisher;
    }

    @Override
    public PageResponse<CourseSummaryResponse> listCourses(CourseFilter filter, Long userId, String role) {
        CourseFilter effectiveFilter = filter == null ? CourseFilter.empty() : filter;
        List<Course> matches = courseRepository.search(
                null,
                effectiveFilter.status(),
                null,
                null,
                effectiveFilter.normalizedSearch());
        List<Course> visible = matches.stream()
                .filter(course -> accessPolicy.canView(course, userId, role))
                .toList();

        int page = effectiveFilter.page();
        int size = effectiveFilter.size();
        int from = (int) Math.min((long) page * size, visible.size());
        int to = Math.min(from + size, visible.size());
        List<CourseSummaryResponse> content = CourseMapper.toSummaries(visible.subList(from, to));
        Page<CourseSummaryResponse> result = new PageImpl<>(content, PageRequest.of(page, size), visible.size());
        return PageResponse.from(result);
    }

    @Override
    public CourseResponse getCourseById(Long courseId, Long userId, String role) {
        Course course = findCourse(courseId);
        accessPolicy.requireCanView(course, userId, role);
        return CourseMapper.toResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse createCourse(CourseCreateRequest request, Long teacherId) {
        if (teacherId == null) {
            throw new ForbiddenOperationException("Identifiant enseignant requis");
        }
        Course course = CourseMapper.toEntity(request, teacherId);
        course.setStatus(CourseStatus.DRAFT);
        Course savedCourse = courseRepository.save(course);
        courseEventPublisher.publishCourseCreated(new CourseCreatedEvent(
                UUID.randomUUID().toString(),
                savedCourse.getId(),
                savedCourse.getTitle(),
                savedCourse.getTeacherId(),
                LocalDateTime.now()));
        return CourseMapper.toResponse(savedCourse);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long courseId, CourseUpdateRequest request, Long userId, String role) {
        Course course = findCourse(courseId);
        accessPolicy.requireCanModify(course, userId, role);
        CourseMapper.updateEntity(course, withoutStatus(request));
        return CourseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseResponse publishCourse(Long courseId, Long userId, String role) {
        Course course = findCourse(courseId);
        accessPolicy.requireCanModify(course, userId, role);
        if (course.getStatus() != CourseStatus.DRAFT) {
            throw new BusinessConflictException("Transition de publication invalide pour ce cours");
        }
        course.setStatus(CourseStatus.PUBLISHED);
        return CourseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseResponse unpublishCourse(Long courseId, Long userId, String role) {
        Course course = findCourse(courseId);
        accessPolicy.requireCanModify(course, userId, role);
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BusinessConflictException("Transition de depublication invalide pour ce cours");
        }
        course.setStatus(CourseStatus.DRAFT);
        return CourseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId, Long userId, String role) {
        Course course = findCourse(courseId);
        accessPolicy.requireCanModify(course, userId, role);
        courseRepository.delete(course);
    }

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours introuvable"));
    }

    private CourseUpdateRequest withoutStatus(CourseUpdateRequest request) {
        if (request == null) {
            return new CourseUpdateRequest(null, null, null, null, null, null);
        }
        return new CourseUpdateRequest(request.title(), request.description(), request.category(),
                request.level(), null, request.price());
    }
}
