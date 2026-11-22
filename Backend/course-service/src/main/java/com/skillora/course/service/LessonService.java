package com.skillora.course.service;

import com.skillora.course.api.dto.LessonCreateRequest;
import com.skillora.course.api.dto.LessonResponse;
import com.skillora.course.api.dto.LessonUpdateRequest;

import java.util.List;

public interface LessonService {

    List<LessonResponse> listLessons(Long courseId, Long userId, String role);

    LessonResponse addLesson(Long courseId, LessonCreateRequest request, Long userId, String role);

    LessonResponse updateLesson(Long lessonId, LessonUpdateRequest request, Long userId, String role);

    LessonResponse reorderLesson(Long lessonId, Integer newPosition, Long userId, String role);

    void deleteLesson(Long lessonId, Long userId, String role);
}
