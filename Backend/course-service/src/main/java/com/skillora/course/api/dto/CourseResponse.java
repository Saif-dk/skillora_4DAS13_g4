package com.skillora.course.api.dto;

import com.skillora.course.domain.enumeration.CourseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CourseResponse(
        Long id,
        String title,
        String description,
        String category,
        String level,
        CourseStatus status,
        BigDecimal price,
        Long teacherId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<LessonResponse> lessons,
        List<ResourceResponse> resources
) {
}
