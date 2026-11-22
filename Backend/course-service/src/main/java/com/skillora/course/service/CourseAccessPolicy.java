package com.skillora.course.service;

import com.skillora.course.api.exception.ForbiddenOperationException;
import com.skillora.course.domain.entity.Course;
import com.skillora.course.domain.enumeration.CourseStatus;
import org.springframework.stereotype.Component;

@Component
public class CourseAccessPolicy {

    private static final String ADMIN_ROLE = "ADMIN";

    public boolean isAdmin(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        String normalized = role.trim().toUpperCase(java.util.Locale.ROOT);
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }
        return ADMIN_ROLE.equals(normalized);
    }

    public boolean canView(Course course, Long userId, String role) {
        if (course == null) {
            return false;
        }
        return isAdmin(role)
                || isOwner(course, userId)
                || course.getStatus() == CourseStatus.PUBLISHED;
    }

    public boolean canModify(Course course, Long userId, String role) {
        if (course == null) {
            return false;
        }
        return isAdmin(role) || isOwner(course, userId);
    }

    public void requireCanView(Course course, Long userId, String role) {
        if (!canView(course, userId, role)) {
            throw new ForbiddenOperationException("Acces interdit a ce cours");
        }
    }

    public void requireCanModify(Course course, Long userId, String role) {
        if (!canModify(course, userId, role)) {
            throw new ForbiddenOperationException("Modification interdite sur ce cours");
        }
    }

    private boolean isOwner(Course course, Long userId) {
        return userId != null && userId.equals(course.getTeacherId());
    }
}
