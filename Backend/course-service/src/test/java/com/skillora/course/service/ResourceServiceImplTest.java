package com.skillora.course.service;

import com.skillora.course.api.dto.ResourceCreateRequest;
import com.skillora.course.api.dto.ResourceResponse;
import com.skillora.course.api.exception.BusinessConflictException;
import com.skillora.course.api.exception.ForbiddenOperationException;
import com.skillora.course.domain.entity.Course;
import com.skillora.course.domain.entity.Lesson;
import com.skillora.course.domain.entity.Resource;
import com.skillora.course.domain.enumeration.CourseStatus;
import com.skillora.course.domain.enumeration.ResourceType;
import com.skillora.course.repository.CourseRepository;
import com.skillora.course.repository.LessonRepository;
import com.skillora.course.repository.ResourceRepository;
import com.skillora.course.service.impl.ResourceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceImplTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    private ResourceServiceImpl service;
    private Course course;

    @BeforeEach
    void setUp() {
        service = new ResourceServiceImpl(resourceRepository, courseRepository, lessonRepository,
                new CourseAccessPolicy());
        course = new Course();
        course.setId(1L);
        course.setTeacherId(42L);
        course.setStatus(CourseStatus.PUBLISHED);
        course.setTitle("Java");
    }

    @Test
    void createResourceAttachedToCourse() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResourceResponse response = service.createResource(
                new ResourceCreateRequest(1L, null, "Support", ResourceType.PDF, "https://exemple.fr/pdf", "2MB"),
                42L, "TEACHER");

        assertThat(response.courseId()).isEqualTo(1L);
        assertThat(response.lessonId()).isNull();
        assertThat(response.type()).isEqualTo(ResourceType.PDF);
        assertThat(response.name()).isEqualTo("Support");
    }

    @Test
    void createResourceAttachedToLesson() {
        Lesson lesson = lesson(2L);
        when(lessonRepository.findById(2L)).thenReturn(Optional.of(lesson));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResourceResponse response = service.createResource(
                new ResourceCreateRequest(null, 2L, "Video", ResourceType.VIDEO, "https://exemple.fr/video", null),
                42L, "TEACHER");

        assertThat(response.courseId()).isNull();
        assertThat(response.lessonId()).isEqualTo(2L);
        assertThat(response.name()).isEqualTo("Video");
    }

    @Test
    void createResourceRequiresExactlyOneAttachment() {
        ResourceCreateRequest none = new ResourceCreateRequest(
                null, null, "Support", ResourceType.PDF, "https://exemple.fr/pdf", null);
        ResourceCreateRequest both = new ResourceCreateRequest(
                1L, 2L, "Support", ResourceType.PDF, "https://exemple.fr/pdf", null);

        assertThatThrownBy(() -> service.createResource(none, 42L, "TEACHER"))
                .isInstanceOf(BusinessConflictException.class);
        assertThatThrownBy(() -> service.createResource(both, 42L, "TEACHER"))
                .isInstanceOf(BusinessConflictException.class);
        verifyNoInteractions(resourceRepository);
    }

    @Test
    void createResourceRejectsNonOwnerAndAdminCanOverride() {
        course.setStatus(CourseStatus.DRAFT);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        ResourceCreateRequest request = new ResourceCreateRequest(
                1L, null, "Support", ResourceType.PDF, "https://exemple.fr/pdf", null);

        assertThatThrownBy(() -> service.createResource(request, 7L, "STUDENT"))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(resourceRepository, never()).save(any(Resource.class));

        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(service.createResource(request, 99L, "ADMIN").courseId()).isEqualTo(1L);
    }

    @Test
    void getResourceByIdAppliesAccessCheck() {
        Resource resource = resource(10L, course);
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));

        assertThat(service.getResourceById(10L, 7L, "STUDENT").id()).isEqualTo(10L);

        course.setStatus(CourseStatus.DRAFT);
        assertThatThrownBy(() -> service.getResourceById(10L, 7L, "STUDENT"))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThat(service.getResourceById(10L, 42L, "TEACHER").id()).isEqualTo(10L);
    }

    @Test
    void deleteResourceAppliesAccessCheck() {
        Resource resource = resource(10L, course);
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));

        assertThatThrownBy(() -> service.deleteResource(10L, 7L, "STUDENT"))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(resourceRepository, never()).delete(any(Resource.class));

        service.deleteResource(10L, 42L, "TEACHER");
        verify(resourceRepository).delete(resource);
    }

    private Lesson lesson(Long id) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        lesson.setCourse(course);
        lesson.setTitle("Lecon");
        lesson.setPosition(1);
        return lesson;
    }

    private Resource resource(Long id, Course attachedCourse) {
        Resource resource = new Resource();
        resource.setId(id);
        resource.setCourse(attachedCourse);
        resource.setName("Support");
        resource.setType(ResourceType.PDF);
        resource.setUrl("https://exemple.fr/pdf");
        return resource;
    }
}
