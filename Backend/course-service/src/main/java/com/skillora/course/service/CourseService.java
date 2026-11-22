package com.skillora.course.service;

import com.skillora.course.api.dto.CourseCreateRequest;
import com.skillora.course.api.dto.CourseResponse;
import com.skillora.course.api.dto.CourseSummaryResponse;
import com.skillora.course.api.dto.CourseUpdateRequest;
import com.skillora.course.api.dto.PageResponse;

public interface CourseService {

    PageResponse<CourseSummaryResponse> listCourses(CourseFilter filter, Long userId, String role);

    CourseResponse getCourseById(Long courseId, Long userId, String role);

    CourseResponse createCourse(CourseCreateRequest request, Long teacherId);

    CourseResponse updateCourse(Long courseId, CourseUpdateRequest request, Long userId, String role);

    CourseResponse publishCourse(Long courseId, Long userId, String role);

    CourseResponse unpublishCourse(Long courseId, Long userId, String role);

    void deleteCourse(Long courseId, Long userId, String role);
}
