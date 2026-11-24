package tn.esprit.classe.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cours-service", url = "${services.cours.url}")
public interface CourseClient {

    @GetMapping("/api/v1/courses/{id}")
    CourseDto getCourseById(@PathVariable("id") Long id);
}
