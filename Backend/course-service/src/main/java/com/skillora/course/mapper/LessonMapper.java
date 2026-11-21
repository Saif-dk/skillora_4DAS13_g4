package com.skillora.course.mapper;

import com.skillora.course.api.dto.LessonCreateRequest;
import com.skillora.course.api.dto.LessonResponse;
import com.skillora.course.api.dto.LessonUpdateRequest;
import com.skillora.course.domain.entity.Lesson;

import java.util.List;

public final class LessonMapper {

    private LessonMapper() {
    }

    public static Lesson toEntity(LessonCreateRequest request) {
        Lesson lesson = new Lesson();
        lesson.setTitle(request.title());
        lesson.setContent(request.content());
        lesson.setPosition(request.position());
        return lesson;
    }

    public static void updateEntity(Lesson lesson, LessonUpdateRequest request) {
        if (request.title() != null) {
            lesson.setTitle(request.title());
        }
        if (request.content() != null) {
            lesson.setContent(request.content());
        }
        if (request.position() != null) {
            lesson.setPosition(request.position());
        }
    }

    public static LessonResponse toResponse(Lesson lesson) {
        if (lesson == null) {
            return null;
        }
        return new LessonResponse(
                lesson.getId(),
                lesson.getCourse() == null ? null : lesson.getCourse().getId(),
                lesson.getTitle(),
                lesson.getContent(),
                lesson.getPosition(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt(),
                ResourceMapper.toResponses(lesson.getResources())
        );
    }

    public static List<LessonResponse> toResponses(List<Lesson> lessons) {
        if (lessons == null) {
            return List.of();
        }
        return lessons.stream().map(LessonMapper::toResponse).toList();
    }
}
