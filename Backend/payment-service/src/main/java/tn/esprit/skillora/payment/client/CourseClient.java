package tn.esprit.skillora.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Synchronous call to the Course service: GET /api/courses/{id}.
 * With Eureka the name "course-service" is resolved by the load balancer; for local development set
 * clients.course-service.url to bypass discovery.
 */
@FeignClient(name = "course-service", url = "${clients.course-service.url:}")
public interface CourseClient {

    @GetMapping("/api/courses/{id}")
    CourseDto getCourse(@PathVariable("id") Long id);
}
