package com.skillora.course.repository;

import com.skillora.course.domain.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByCourseId(Long courseId);

    List<Lesson> findByCourseIdOrderByPositionAsc(Long courseId);
}
