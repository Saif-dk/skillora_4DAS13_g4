package com.skillora.course.repository;

import com.skillora.course.domain.entity.Course;
import com.skillora.course.domain.enumeration.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByStatus(CourseStatus status);

    List<Course> findByTeacherId(Long teacherId);

    @Query("""
            select c from Course c
            where (:teacherId is null or c.teacherId = :teacherId)
              and (:status is null or c.status = :status)
              and (:category is null or c.category = :category)
              and (:level is null or c.level = :level)
              and (:search is null
                   or lower(c.title) like lower(concat('%', :search, '%'))
                   or lower(c.description) like lower(concat('%', :search, '%')))
            """)
    List<Course> search(@Param("teacherId") Long teacherId,
                        @Param("status") CourseStatus status,
                        @Param("category") String category,
                        @Param("level") String level,
                        @Param("search") String search);
}
