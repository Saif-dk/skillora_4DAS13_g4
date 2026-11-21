package com.skillora.course.api.dto;

import com.skillora.course.domain.enumeration.CourseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CourseSummaryResponse(
        Long id,
        String title,
        String category,
        String level,
        CourseStatus status,
        BigDecimal price,
        Long teacherId,
        LocalDateTime createdAt
) {
}
