package com.skillora.course.repository;

import com.skillora.course.domain.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findByCourseId(Long courseId);

    List<Resource> findByCourseIdOrderByIdAsc(Long courseId);

    List<Resource> findByLessonId(Long lessonId);

    List<Resource> findByLessonIdOrderByIdAsc(Long lessonId);
}
