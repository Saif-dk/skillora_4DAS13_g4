package com.skillora.course.api.dto;

import com.skillora.course.domain.enumeration.CourseStatus;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CourseCreateRequest(
        @NotBlank @Size(max = 150) String title,
        String description,
        @Size(max = 100) String category,
        @Size(max = 30) String level,
        CourseStatus status,
        @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal price
) {
}
