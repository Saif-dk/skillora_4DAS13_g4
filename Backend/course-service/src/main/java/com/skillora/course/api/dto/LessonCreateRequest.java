package com.skillora.course.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LessonCreateRequest(
        @NotBlank @Size(max = 150) String title,
        String content,
        @NotNull @Positive Integer position
) {
}
