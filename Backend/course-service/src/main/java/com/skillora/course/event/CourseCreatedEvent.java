package com.skillora.course.event;

import java.time.LocalDateTime;

public record CourseCreatedEvent(
        String eventId,
        Long courseId,
        String title,
        Long teacherId,
        LocalDateTime occurredAt
) {
}
