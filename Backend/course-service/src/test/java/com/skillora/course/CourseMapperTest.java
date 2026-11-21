package com.skillora.course;

import com.skillora.course.api.dto.CourseCreateRequest;
import com.skillora.course.api.dto.CourseResponse;
import com.skillora.course.api.dto.CourseSummaryResponse;
import com.skillora.course.api.dto.CourseUpdateRequest;
import com.skillora.course.api.dto.LessonResponse;
import com.skillora.course.api.dto.ResourceResponse;
import com.skillora.course.domain.entity.Course;
import com.skillora.course.domain.entity.Lesson;
import com.skillora.course.domain.entity.Resource;
import com.skillora.course.domain.enumeration.CourseStatus;
import com.skillora.course.domain.enumeration.ResourceType;
import com.skillora.course.mapper.CourseMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CourseMapperTest {

    @Test
    void shouldMapCreateRequestToEntityWithDefaultStatus() {
        CourseCreateRequest request = new CourseCreateRequest(
                "Java", "Description", "Developpement", "DEBUTANT", null, new BigDecimal("49.90"));

        Course course = CourseMapper.toEntity(request, 42L);

        assertThat(course.getTitle()).isEqualTo("Java");
        assertThat(course.getDescription()).isEqualTo("Description");
        assertThat(course.getTeacherId()).isEqualTo(42L);
        assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);
        assertThat(course.getPrice()).isEqualByComparingTo("49.90");
    }

    @Test
    void shouldMapEntityToResponseWithNestedLessonsAndResources() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 10, 9, 0);
        Course course = new Course();
        course.setId(1L);
        course.setTitle("Java");
        course.setDescription("Description");
        course.setCategory("Developpement");
        course.setLevel("DEBUTANT");
        course.setStatus(CourseStatus.PUBLISHED);
        course.setTeacherId(42L);
        course.setPrice(new BigDecimal("49.90"));
        course.setCreatedAt(createdAt);

        Lesson lesson = new Lesson();
        lesson.setId(10L);
        lesson.setTitle("Introduction");
        lesson.setPosition(1);
        course.addLesson(lesson);

        Resource lessonResource = new Resource();
        lessonResource.setId(100L);
        lessonResource.setName("Video");
        lessonResource.setType(ResourceType.VIDEO);
        lessonResource.setUrl("https://exemple.fr/video");
        lesson.addResource(lessonResource);

        Resource courseResource = new Resource();
        courseResource.setId(101L);
        courseResource.setName("Support");
        courseResource.setType(ResourceType.PDF);
        courseResource.setUrl("https://exemple.fr/support");
        course.addResource(courseResource);

        CourseResponse response = CourseMapper.toResponse(course);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.teacherId()).isEqualTo(42L);
        assertThat(response.lessons()).hasSize(1);
        LessonResponse mappedLesson = response.lessons().get(0);
        assertThat(mappedLesson.courseId()).isEqualTo(1L);
        assertThat(mappedLesson.resources()).extracting(ResourceResponse::name).containsExactly("Video");
        assertThat(response.resources()).extracting(ResourceResponse::id).containsExactly(101L);

        CourseSummaryResponse summary = CourseMapper.toSummary(course);
        assertThat(summary.title()).isEqualTo("Java");
        assertThat(summary.status()).isEqualTo(CourseStatus.PUBLISHED);
        assertThat(summary.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldUpdateEntityWithoutChangingTeacherId() {
        Course course = CourseMapper.toEntity(
                new CourseCreateRequest("Java", "Description", "Dev", "DEBUTANT", null, new BigDecimal("49.90")),
                42L);

        CourseMapper.updateEntity(course, new CourseUpdateRequest(
                "Java Pro", null, null, "AVANCE", CourseStatus.PUBLISHED, new BigDecimal("59.90")));

        assertThat(course.getTitle()).isEqualTo("Java Pro");
        assertThat(course.getDescription()).isEqualTo("Description");
        assertThat(course.getLevel()).isEqualTo("AVANCE");
        assertThat(course.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
        assertThat(course.getPrice()).isEqualByComparingTo("59.90");
        assertThat(course.getTeacherId()).isEqualTo(42L);
    }
}
