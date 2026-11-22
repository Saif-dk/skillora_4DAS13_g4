package com.skillora.course.service;

import com.skillora.course.api.exception.ForbiddenOperationException;
import com.skillora.course.domain.entity.Course;
import com.skillora.course.domain.enumeration.CourseStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourseAccessPolicyTest {

    private final CourseAccessPolicy policy = new CourseAccessPolicy();

    @Test
    void adminCanViewAndModifyAnyCourse() {
        Course draft = course(99L, CourseStatus.DRAFT);

        assertThat(policy.canView(draft, 1L, "ADMIN")).isTrue();
        assertThat(policy.canModify(draft, 1L, "ADMIN")).isTrue();
        assertThat(policy.canView(draft, 1L, "ROLE_ADMIN")).isTrue();
        assertThat(policy.canModify(draft, 1L, "role_admin")).isTrue();
    }

    @Test
    void ownerCanViewAndModifyOwnCourseEvenWhenDraft() {
        Course draft = course(42L, CourseStatus.DRAFT);

        assertThat(policy.canView(draft, 42L, "TEACHER")).isTrue();
        assertThat(policy.canModify(draft, 42L, "TEACHER")).isTrue();
    }

    @Test
    void otherUsersCanOnlyViewPublishedCourses() {
        Course draft = course(42L, CourseStatus.DRAFT);
        Course published = course(42L, CourseStatus.PUBLISHED);

        assertThat(policy.canView(draft, 7L, "STUDENT")).isFalse();
        assertThat(policy.canModify(draft, 7L, "STUDENT")).isFalse();
        assertThat(policy.canView(published, 7L, "STUDENT")).isTrue();
        assertThat(policy.canModify(published, 7L, "TEACHER")).isFalse();
        assertThat(policy.canView(null, 7L, "ADMIN")).isFalse();
        assertThat(policy.canModify(null, 7L, "ADMIN")).isFalse();
    }

    @Test
    void requireMethodsThrowForbiddenOperationException() {
        Course draft = course(42L, CourseStatus.DRAFT);

        assertThatThrownBy(() -> policy.requireCanView(draft, 7L, "STUDENT"))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThatThrownBy(() -> policy.requireCanModify(draft, 7L, "STUDENT"))
                .isInstanceOf(ForbiddenOperationException.class);
        policy.requireCanModify(draft, 42L, "TEACHER");
        policy.requireCanView(draft, 7L, "ADMIN");
    }

    private Course course(Long teacherId, CourseStatus status) {
        Course course = new Course();
        course.setId(1L);
        course.setTeacherId(teacherId);
        course.setStatus(status);
        return course;
    }
}
