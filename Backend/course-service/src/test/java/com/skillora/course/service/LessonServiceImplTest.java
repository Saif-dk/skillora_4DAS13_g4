package com.skillora.course.service;

import com.skillora.course.api.dto.LessonCreateRequest;
import com.skillora.course.api.dto.LessonResponse;
import com.skillora.course.api.dto.LessonUpdateRequest;
import com.skillora.course.api.exception.ForbiddenOperationException;
import com.skillora.course.domain.entity.Course;
import com.skillora.course.domain.entity.Lesson;
import com.skillora.course.domain.enumeration.CourseStatus;
import com.skillora.course.repository.CourseRepository;
import com.skillora.course.repository.LessonRepository;
import com.skillora.course.service.impl.LessonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonServiceImplTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseRepository courseRepository;

    private LessonServiceImpl service;
    private Course course;

    @BeforeEach
    void setUp() {
        service = new LessonServiceImpl(lessonRepository, courseRepository, new CourseAccessPolicy());
        course = new Course();
        course.setId(1L);
        course.setTeacherId(42L);
        course.setStatus(CourseStatus.DRAFT);
        course.setTitle("Java");
    }

    @Test
    void addLessonChecksAccessAndShiftsExistingPositions() {
        Lesson existing = lesson(10L, 1);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(lessonRepository.findByCourseIdOrderByPositionAsc(1L)).thenReturn(List.of(existing));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LessonResponse response = service.addLesson(1L,
                new LessonCreateRequest("Setup", "Contenu", 1), 42L, "TEACHER");

        assertThat(response.courseId()).isEqualTo(1L);
        assertThat(response.position()).isEqualTo(1);
        assertThat(existing.getPosition()).isEqualTo(2);
        verify(lessonRepository).saveAll(List.of(existing));
    }

    @Test
    void addLessonAppendsWhenPositionNotProvided() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(lessonRepository.findByCourseIdOrderByPositionAsc(1L))
                .thenReturn(List.of(lesson(10L, 1), lesson(11L, 2)));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LessonResponse response = service.addLesson(1L,
                new LessonCreateRequest("Conclusion", "Contenu", null), 42L, "TEACHER");

        assertThat(response.position()).isEqualTo(3);
    }

    @Test
    void addLessonRejectsNonOwner() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> service.addLesson(1L,
                new LessonCreateRequest("Setup", "Contenu", 1), 7L, "STUDENT"))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void updateLessonMovesPositionAndUpdatesContent() {
        Lesson target = lesson(11L, 2);
        Lesson first = lesson(10L, 1);
        when(lessonRepository.findById(11L)).thenReturn(Optional.of(target));
        when(lessonRepository.findByCourseIdOrderByPositionAsc(1L)).thenReturn(List.of(first, target));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LessonResponse response = service.updateLesson(11L,
                new LessonUpdateRequest("Intro modifiee", "Nouveau contenu", 1), 42L, "TEACHER");

        assertThat(response.position()).isEqualTo(1);
        assertThat(response.title()).isEqualTo("Intro modifiee");
        assertThat(target.getPosition()).isEqualTo(1);
        assertThat(first.getPosition()).isEqualTo(2);
        assertThatThrownBy(() -> service.updateLesson(11L,
                new LessonUpdateRequest("Interdit", null, 1), 7L, "STUDENT"))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void reorderLessonChangesPosition() {
        Lesson target = lesson(10L, 1);
        Lesson second = lesson(11L, 2);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(target));
        when(lessonRepository.findByCourseIdOrderByPositionAsc(1L)).thenReturn(List.of(target, second));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LessonResponse response = service.reorderLesson(10L, 2, 42L, "TEACHER");

        assertThat(response.position()).isEqualTo(2);
        assertThat(second.getPosition()).isEqualTo(1);
    }

    @Test
    void deleteLessonChecksAccessAndDeletes() {
        Lesson target = lesson(10L, 1);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(target));
        when(lessonRepository.findByCourseIdOrderByPositionAsc(1L))
                .thenReturn(List.of(lesson(11L, 2)));

        assertThatThrownBy(() -> service.deleteLesson(10L, 7L, "STUDENT"))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(lessonRepository, never()).delete(any(Lesson.class));

        service.deleteLesson(10L, 42L, "TEACHER");
        verify(lessonRepository).delete(target);
    }

    private Lesson lesson(Long id, int position) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        lesson.setCourse(course);
        lesson.setTitle("Lecon " + id);
        lesson.setPosition(position);
        return lesson;
    }
}
