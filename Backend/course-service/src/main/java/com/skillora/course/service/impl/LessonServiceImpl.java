package com.skillora.course.service.impl;

import com.skillora.course.api.dto.LessonCreateRequest;
import com.skillora.course.api.dto.LessonResponse;
import com.skillora.course.api.dto.LessonUpdateRequest;
import com.skillora.course.api.exception.ResourceNotFoundException;
import com.skillora.course.domain.entity.Course;
import com.skillora.course.domain.entity.Lesson;
import com.skillora.course.mapper.LessonMapper;
import com.skillora.course.repository.CourseRepository;
import com.skillora.course.repository.LessonRepository;
import com.skillora.course.service.CourseAccessPolicy;
import com.skillora.course.service.LessonService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final CourseAccessPolicy accessPolicy;

    public LessonServiceImpl(LessonRepository lessonRepository,
                             CourseRepository courseRepository,
                             CourseAccessPolicy accessPolicy) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.accessPolicy = accessPolicy;
    }

    @Override
    public List<LessonResponse> listLessons(Long courseId, Long userId, String role) {
        Course course = findCourse(courseId);
        accessPolicy.requireCanView(course, userId, role);
        return LessonMapper.toResponses(lessonRepository.findByCourseIdOrderByPositionAsc(courseId));
    }

    @Override
    @Transactional
    public LessonResponse addLesson(Long courseId, LessonCreateRequest request, Long userId, String role) {
        Course course = findCourse(courseId);
        accessPolicy.requireCanModify(course, userId, role);

        int position = request.position() == null ? nextPosition(courseId) : request.position();
        shiftLessonsFrom(courseId, position);

        Lesson lesson = LessonMapper.toEntity(request);
        lesson.setPosition(position);
        lesson.setCourse(course);
        return LessonMapper.toResponse(lessonRepository.save(lesson));
    }

    @Override
    @Transactional
    public LessonResponse updateLesson(Long lessonId, LessonUpdateRequest request, Long userId, String role) {
        Lesson lesson = findLesson(lessonId);
        Course course = requireCourse(lesson);
        accessPolicy.requireCanModify(course, userId, role);

        int oldPosition = lesson.getPosition() == null ? 0 : lesson.getPosition();
        int newPosition = request.position() == null ? oldPosition : request.position();
        if (newPosition != oldPosition) {
            moveLesson(lesson, course.getId(), newPosition);
        }
        LessonMapper.updateEntity(lesson, new LessonUpdateRequest(
                request.title(), request.content(), lesson.getPosition()));
        return LessonMapper.toResponse(lessonRepository.save(lesson));
    }

    @Override
    @Transactional
    public LessonResponse reorderLesson(Long lessonId, Integer newPosition, Long userId, String role) {
        Lesson lesson = findLesson(lessonId);
        LessonUpdateRequest request = new LessonUpdateRequest(
                lesson.getTitle(), lesson.getContent(), newPosition);
        return updateLesson(lessonId, request, userId, role);
    }

    @Override
    @Transactional
    public void deleteLesson(Long lessonId, Long userId, String role) {
        Lesson lesson = findLesson(lessonId);
        Course course = requireCourse(lesson);
        accessPolicy.requireCanModify(course, userId, role);

        lessonRepository.delete(lesson);
        lessonRepository.flush();
        renumber(lessonRepository.findByCourseIdOrderByPositionAsc(course.getId()));
    }

    private void moveLesson(Lesson lesson, Long courseId, int newPosition) {
        List<Lesson> others = lessonRepository.findByCourseIdOrderByPositionAsc(courseId).stream()
                .filter(other -> !sameId(other.getId(), lesson.getId()))
                .toList();

        int insertIndex = Math.max(0, Math.min(newPosition - 1, others.size()));
        List<Lesson> shifted = new ArrayList<>();
        for (int i = 0; i < others.size(); i++) {
            Lesson other = others.get(i);
            other.setPosition(i < insertIndex ? i + 1 : i + 2);
            shifted.add(other);
        }
        if (!shifted.isEmpty()) {
            lessonRepository.saveAll(shifted);
        }
        lesson.setPosition(insertIndex + 1);
    }

    private void shiftLessonsFrom(Long courseId, int position) {
        List<Lesson> toShift = lessonRepository.findByCourseIdOrderByPositionAsc(courseId).stream()
                .filter(lesson -> lesson.getPosition() != null && lesson.getPosition() >= position)
                .toList();
        toShift.forEach(lesson -> lesson.setPosition(lesson.getPosition() + 1));
        if (!toShift.isEmpty()) {
            lessonRepository.saveAll(toShift);
        }
    }

    private void renumber(List<Lesson> lessons) {
        for (int i = 0; i < lessons.size(); i++) {
            lessons.get(i).setPosition(i + 1);
        }
        if (!lessons.isEmpty()) {
            lessonRepository.saveAll(lessons);
        }
    }

    private int nextPosition(Long courseId) {
        return lessonRepository.findByCourseIdOrderByPositionAsc(courseId).stream()
                .map(Lesson::getPosition)
                .filter(position -> position != null)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours introuvable"));
    }

    private Lesson findLesson(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecon introuvable"));
    }

    private Course requireCourse(Lesson lesson) {
        if (lesson.getCourse() == null) {
            throw new ResourceNotFoundException("Cours introuvable pour cette lecon");
        }
        return lesson.getCourse();
    }

    private boolean sameId(Long first, Long second) {
        return first != null && first.equals(second);
    }
}
