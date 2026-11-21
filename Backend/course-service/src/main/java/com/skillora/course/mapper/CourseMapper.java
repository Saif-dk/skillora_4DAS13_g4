package com.skillora.course.mapper;

import com.skillora.course.api.dto.CourseCreateRequest;
import com.skillora.course.api.dto.CourseResponse;
import com.skillora.course.api.dto.CourseSummaryResponse;
import com.skillora.course.api.dto.CourseUpdateRequest;
import com.skillora.course.domain.entity.Course;
import com.skillora.course.domain.enumeration.CourseStatus;

import java.util.List;

public final class CourseMapper {

    private CourseMapper() {
    }

    public static Course toEntity(CourseCreateRequest request, Long teacherId) {
        Course course = new Course();
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setCategory(request.category());
        course.setLevel(request.level());
        course.setStatus(request.status() == null ? CourseStatus.DRAFT : request.status());
        course.setTeacherId(teacherId);
        course.setPrice(request.price());
        return course;
    }

    public static void updateEntity(Course course, CourseUpdateRequest request) {
        if (request.title() != null) {
            course.setTitle(request.title());
        }
        if (request.description() != null) {
            course.setDescription(request.description());
        }
        if (request.category() != null) {
            course.setCategory(request.category());
        }
        if (request.level() != null) {
            course.setLevel(request.level());
        }
        if (request.status() != null) {
            course.setStatus(request.status());
        }
        if (request.price() != null) {
            course.setPrice(request.price());
        }
    }

    public static CourseResponse toResponse(Course course) {
        if (course == null) {
            return null;
        }
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCategory(),
                course.getLevel(),
                course.getStatus(),
                course.getPrice(),
                course.getTeacherId(),
                course.getCreatedAt(),
                course.getUpdatedAt(),
                LessonMapper.toResponses(course.getLessons()),
                ResourceMapper.toResponses(course.getResources())
        );
    }

    public static CourseSummaryResponse toSummary(Course course) {
        if (course == null) {
            return null;
        }
        return new CourseSummaryResponse(
                course.getId(),
                course.getTitle(),
                course.getCategory(),
                course.getLevel(),
                course.getStatus(),
                course.getPrice(),
                course.getTeacherId(),
                course.getCreatedAt()
        );
    }

    public static List<CourseSummaryResponse> toSummaries(List<Course> courses) {
        if (courses == null) {
            return List.of();
        }
        return courses.stream().map(CourseMapper::toSummary).toList();
    }
}
