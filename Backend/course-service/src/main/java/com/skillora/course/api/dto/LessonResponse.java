package com.skillora.course.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LessonResponse(
        Long id,
        Long courseId,
        String title,
        String content,
        Integer position,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ResourceResponse> resources
) {
}
