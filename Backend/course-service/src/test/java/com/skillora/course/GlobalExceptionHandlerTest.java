package com.skillora.course;

import com.skillora.course.api.dto.CourseCreateRequest;
import com.skillora.course.api.exception.BusinessConflictException;
import com.skillora.course.api.exception.ForbiddenOperationException;
import com.skillora.course.api.exception.GlobalExceptionHandler;
import com.skillora.course.api.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController(), new GlobalExceptionHandler()).build();
    }

    @Test
    void shouldMapResourceNotFoundExceptionToNotFound() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Cours introuvable"))
                .andExpect(jsonPath("$.path").value("/test/not-found"))
                .andExpect(jsonPath("$.instant").exists());
    }

    @Test
    void shouldMapBusinessConflictExceptionToConflict() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Cours deja publie"));
    }

    @Test
    void shouldMapForbiddenOperationExceptionToForbidden() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Operation interdite"));
    }

    @Test
    void shouldMapBeanValidationErrorsToBadRequest() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("title")))
                .andExpect(jsonPath("$.path").value("/test/validation"));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/not-found")
        public void notFound() {
            throw new ResourceNotFoundException("Cours introuvable");
        }

        @GetMapping("/test/conflict")
        public void conflict() {
            throw new BusinessConflictException("Cours deja publie");
        }

        @GetMapping("/test/forbidden")
        public void forbidden() {
            throw new ForbiddenOperationException("Operation interdite");
        }

        @PostMapping("/test/validation")
        public void validate(@Valid @RequestBody CourseCreateRequest request) {
        }
    }
}
