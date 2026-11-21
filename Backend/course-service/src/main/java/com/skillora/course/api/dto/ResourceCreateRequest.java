package com.skillora.course.api.dto;

import com.skillora.course.domain.enumeration.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ResourceCreateRequest(
        @Positive Long courseId,
        @Positive Long lessonId,
        @NotBlank @Size(max = 200) String name,
        @NotNull ResourceType type,
        @NotBlank @Size(max = 500) String url,
        @Size(max = 50) String size
) {
}
