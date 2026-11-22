package com.skillora.course.service;

import com.skillora.course.domain.enumeration.CourseStatus;

public record CourseFilter(String search, CourseStatus status, int page, int size) {

    public static final int DEFAULT_SIZE = 20;

    public CourseFilter {
        page = Math.max(page, 0);
        size = size < 1 ? DEFAULT_SIZE : size;
    }

    public static CourseFilter of(String search, CourseStatus status, Integer page, Integer size) {
        return new CourseFilter(search, status,
                page == null ? 0 : page,
                size == null ? DEFAULT_SIZE : size);
    }

    public static CourseFilter empty() {
        return new CourseFilter(null, null, 0, DEFAULT_SIZE);
    }

    public String normalizedSearch() {
        return search == null || search.isBlank() ? null : search.trim();
    }
}
