package com.skillora.course;

import com.skillora.course.domain.entity.Course;
import com.skillora.course.domain.entity.Lesson;
import com.skillora.course.domain.entity.Resource;
import com.skillora.course.domain.enumeration.CourseStatus;
import com.skillora.course.domain.enumeration.ResourceType;
import com.skillora.course.repository.CourseRepository;
import com.skillora.course.repository.LessonRepository;
import com.skillora.course.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Test
    void shouldFindCoursesByStatusTeacherAndSearch() {
        courseRepository.saveAll(List.of(
                newCourse("Java de base", CourseStatus.DRAFT, 1L),
                newCourse("Spring avance", CourseStatus.PUBLISHED, 2L)
        ));
        courseRepository.flush();

        assertThat(courseRepository.findByStatus(CourseStatus.DRAFT))
                .extracting(Course::getTitle)
                .containsExactly("Java de base");
        assertThat(courseRepository.findByTeacherId(2L))
                .extracting(Course::getTitle)
                .containsExactly("Spring avance");
        assertThat(courseRepository.search(null, null, null, null, "java"))
                .extracting(Course::getTitle)
                .containsExactly("Java de base");
    }

    @Test
    void shouldFindLessonsAndResourcesByRelationships() {
        Course course = newCourse("Java", CourseStatus.DRAFT, 1L);

        Lesson setup = new Lesson();
        setup.setTitle("Setup");
        setup.setPosition(1);
        course.addLesson(setup);

        Lesson intro = new Lesson();
        intro.setTitle("Intro");
        intro.setPosition(2);
        course.addLesson(intro);

        Resource video = newResource("Video", ResourceType.VIDEO);
        setup.addResource(video);

        Resource support = newResource("Support", ResourceType.PDF);
        course.addResource(support);

        courseRepository.saveAndFlush(course);

        assertThat(lessonRepository.findByCourseIdOrderByPositionAsc(course.getId()))
                .extracting(Lesson::getTitle)
                .containsExactly("Setup", "Intro");
        assertThat(resourceRepository.findByLessonId(setup.getId()))
                .extracting(Resource::getName)
                .containsExactly("Video");
        assertThat(resourceRepository.findByCourseId(course.getId()))
                .extracting(Resource::getName)
                .containsExactly("Support");
    }

    private Course newCourse(String title, CourseStatus status, Long teacherId) {
        Course course = new Course();
        course.setTitle(title);
        course.setDescription("Description de " + title);
        course.setCategory("Developpement");
        course.setLevel("DEBUTANT");
        course.setStatus(status);
        course.setTeacherId(teacherId);
        course.setPrice(new BigDecimal("49.90"));
        return course;
    }

    private Resource newResource(String name, ResourceType type) {
        Resource resource = new Resource();
        resource.setName(name);
        resource.setType(type);
        resource.setUrl("https://exemple.fr/" + name.toLowerCase());
        return resource;
    }
}
