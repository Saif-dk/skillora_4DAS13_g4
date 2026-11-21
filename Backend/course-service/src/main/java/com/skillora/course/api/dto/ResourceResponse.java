package com.skillora.course.api.dto;

import com.skillora.course.domain.enumeration.ResourceType;

import java.time.LocalDateTime;

public record ResourceResponse(
        Long id,
        Long courseId,
        Long lessonId,
        String name,
        ResourceType type,
        String url,
        String size,
        LocalDateTime createdAt
) {
}
