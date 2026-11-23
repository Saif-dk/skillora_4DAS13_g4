package tn.esprit.skillora.payment.client;

import tn.esprit.skillora.payment.exception.BadRequestException;
import tn.esprit.skillora.payment.exception.ConflictException;
import tn.esprit.skillora.payment.exception.ResourceNotFoundException;
import tn.esprit.skillora.payment.exception.ServiceUnavailableException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Wraps the Feign client: translates remote failures into this service's own exceptions. */
@Component
public class CourseCatalog {

    private static final Logger log = LoggerFactory.getLogger(CourseCatalog.class);
    private static final String PUBLISHED = "PUBLISHED";

    private final CourseClient courseClient;

    public CourseCatalog(CourseClient courseClient) {
        this.courseClient = courseClient;
    }

    /** Returns the course if it exists, is published and has a price to pay. */
    public CourseDto getPayableCourse(Long courseId) {
        CourseDto course;
        try {
            course = courseClient.getCourse(courseId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Course " + courseId + " not found");
        } catch (FeignException e) {
            log.warn("Course service call failed (status {}): {}", e.status(), e.getMessage());
            throw new ServiceUnavailableException("Course service is currently unavailable", e);
        }

        if (course == null || course.id() == null) {
            throw new ServiceUnavailableException("Course service returned an empty response");
        }
        if (course.status() != null && !PUBLISHED.equalsIgnoreCase(course.status())) {
            throw new ConflictException("Course " + courseId + " is not available for purchase");
        }
        if (course.price() == null || course.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Course " + courseId + " has no price to pay");
        }
        return course;
    }
}
