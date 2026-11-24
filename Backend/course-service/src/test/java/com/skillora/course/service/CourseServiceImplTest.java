package com.skillora.course.service;

import com.skillora.course.api.dto.CourseCreateRequest;
import com.skillora.course.api.dto.CourseResponse;
import com.skillora.course.api.dto.CourseSummaryResponse;
import com.skillora.course.api.dto.CourseUpdateRequest;
import com.skillora.course.api.dto.PageResponse;
import com.skillora.course.api.exception.BusinessConflictException;
import com.skillora.course.api.exception.ForbiddenOperationException;
import com.skillora.course.domain.entity.Course;
import com.skillora.course.domain.enumeration.CourseStatus;
import com.skillora.course.event.CourseCreatedEvent;
import com.skillora.course.event.CourseEventPublisher;
import com.skillora.course.repository.CourseRepository;
import com.skillora.course.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseEventPublisher courseEventPublisher;

    private CourseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CourseServiceImpl(courseRepository, new CourseAccessPolicy(), courseEventPublisher);
    }

    @Test
    void studentSeesOnlyPublishedCoursesWhileOwnerAlsoSeesOwnDrafts() {
        Course published = course(1L, 99L, CourseStatus.PUBLISHED);
        Course otherDraft = course(2L, 99L, CourseStatus.DRAFT);
        Course ownDraft = course(3L, 7L, CourseStatus.DRAFT);
        when(courseRepository.search(null, null, null, null, null))
                .thenReturn(List.of(published, otherDraft, ownDraft));

        PageResponse<CourseSummaryResponse> page =
                service.listCourses(new CourseFilter(null, null, 0, 2), 7L, "STUDENT");

        assertThat(page.content()).extracting(CourseSummaryResponse::id).containsExactly(1L, 3L);
        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.totalPages()).isEqualTo(1);
        assertThat(page.page()).isZero();
        assertThat(page.size()).isEqualTo(2);
    }

    @Test
    void adminSeesAllCoursesAndPaginationApplies() {
        when(courseRepository.search(null, null, null, null, "java"))
                .thenReturn(List.of(
                        course(1L, 99L, CourseStatus.DRAFT),
                        course(2L, 99L, CourseStatus.PUBLISHED),
                        course(3L, 99L, CourseStatus.ARCHIVED)));

        PageResponse<CourseSummaryResponse> page =
                service.listCourses(new CourseFilter(" java ", null, 1, 2), null, "ADMIN");

        assertThat(page.content()).extracting(CourseSummaryResponse::id).containsExactly(3L);
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.totalPages()).isEqualTo(2);
        assertThat(page.page()).isEqualTo(1);
    }

    @Test
    void getCourseByIdAppliesAccessCheck() {
        Course draft = course(1L, 42L, CourseStatus.DRAFT);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(draft));

        assertThat(service.getCourseById(1L, 42L, "TEACHER").id()).isEqualTo(1L);
        assertThatThrownBy(() -> service.getCourseById(1L, 7L, "STUDENT"))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void createForcesDraftAndUsesCallerTeacherId() {
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.setId(10L);
            return course;
        });
        CourseCreateRequest request = new CourseCreateRequest(
                "Java", "Description", "Dev", "DEBUTANT", CourseStatus.PUBLISHED, BigDecimal.TEN);

        CourseResponse response = service.createCourse(request, 55L);

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(captor.capture());
        assertThat(response.status()).isEqualTo(CourseStatus.DRAFT);
        assertThat(response.teacherId()).isEqualTo(55L);
        assertThat(captor.getValue().getStatus()).isEqualTo(CourseStatus.DRAFT);
        assertThat(captor.getValue().getTeacherId()).isEqualTo(55L);

        ArgumentCaptor<CourseCreatedEvent> eventCaptor = ArgumentCaptor.forClass(CourseCreatedEvent.class);
        verify(courseEventPublisher).publishCourseCreated(eventCaptor.capture());
        CourseCreatedEvent event = eventCaptor.getValue();
        assertThat(event.eventId()).isNotBlank();
        assertThat(event.courseId()).isEqualTo(10L);
        assertThat(event.title()).isEqualTo("Java");
        assertThat(event.teacherId()).isEqualTo(55L);
        assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    void updateChecksOwnershipAndIgnoresStatusFromBody() {
        Course course = course(1L, 42L, CourseStatus.DRAFT);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CourseUpdateRequest request = new CourseUpdateRequest(
                "Java Pro", null, null, null, CourseStatus.PUBLISHED, BigDecimal.TEN);

        CourseResponse response = service.updateCourse(1L, request, 42L, "TEACHER");

        assertThat(response.title()).isEqualTo("Java Pro");
        assertThat(response.status()).isEqualTo(CourseStatus.DRAFT);
        assertThatThrownBy(() -> service.updateCourse(1L, request, 7L, "STUDENT"))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(courseRepository, never()).delete(any(Course.class));
    }

    @Test
    void publishAndUnpublishRejectInvalidTransitions() {
        Course draft = course(1L, 42L, CourseStatus.DRAFT);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(draft));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.publishCourse(1L, 42L, "TEACHER").status()).isEqualTo(CourseStatus.PUBLISHED);
        assertThatThrownBy(() -> service.publishCourse(1L, 42L, "TEACHER"))
                .isInstanceOf(BusinessConflictException.class);

        assertThat(service.unpublishCourse(1L, 42L, "TEACHER").status()).isEqualTo(CourseStatus.DRAFT);
        draft.setStatus(CourseStatus.ARCHIVED);
        assertThatThrownBy(() -> service.unpublishCourse(1L, 42L, "TEACHER"))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void adminCanPublishOtherTeachersCourse() {
        Course draft = course(1L, 42L, CourseStatus.DRAFT);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(draft));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.publishCourse(1L, 99L, "ADMIN").status()).isEqualTo(CourseStatus.PUBLISHED);
    }

    @Test
    void deleteChecksOwnership() {
        Course course = course(1L, 42L, CourseStatus.DRAFT);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> service.deleteCourse(1L, 7L, "STUDENT"))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(courseRepository, never()).delete(any(Course.class));

        service.deleteCourse(1L, 42L, "TEACHER");
        verify(courseRepository).delete(course);
    }

    private Course course(Long id, Long teacherId, CourseStatus status) {
        Course course = new Course();
        course.setId(id);
        course.setTitle("Java");
        course.setDescription("Description");
        course.setCategory("Dev");
        course.setLevel("DEBUTANT");
        course.setStatus(status);
        course.setTeacherId(teacherId);
        course.setPrice(new BigDecimal("49.90"));
        course.setCreatedAt(LocalDateTime.of(2026, 1, 10, 9, 0));
        return course;
    }
}
