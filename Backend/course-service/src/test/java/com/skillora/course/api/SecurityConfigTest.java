package com.skillora.course.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillora.course.api.dto.CourseCreateRequest;
import com.skillora.course.api.dto.CourseResponse;
import com.skillora.course.api.dto.PageResponse;
import com.skillora.course.domain.enumeration.CourseStatus;
import com.skillora.course.security.JwtService;
import com.skillora.course.service.CourseFilter;
import com.skillora.course.service.CourseService;
import com.skillora.course.service.LessonService;
import com.skillora.course.service.ResourceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private LessonService lessonService;

    @MockitoBean
    private ResourceService resourceService;

    @Test
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void missingTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(courseService);
    }

    @Test
    void invalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/courses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(courseService);
    }

    @Test
    void studentCanReadCourses() throws Exception {
        when(courseService.listCourses(any(CourseFilter.class), eq(7L), eq("STUDENT")))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/v1/courses")
                        .header(HttpHeaders.AUTHORIZATION, bearer("etudiant@skillora.fr", 7L, "STUDENT")))
                .andExpect(status().isOk());
    }

    @Test
    void studentCannotMutateCourses() throws Exception {
        mockMvc.perform(post("/api/v1/courses")
                        .header(HttpHeaders.AUTHORIZATION, bearer("etudiant@skillora.fr", 7L, "STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(courseService);
    }

    @Test
    void professorCanMutateCourses() throws Exception {
        when(courseService.createCourse(any(CourseCreateRequest.class), eq(42L))).thenReturn(course(1L));

        mockMvc.perform(post("/api/v1/courses")
                        .header(HttpHeaders.AUTHORIZATION, bearer("prof@skillora.fr", 42L, "PROFESSOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated());

        verify(courseService).createCourse(any(CourseCreateRequest.class), eq(42L));
    }

    @Test
    void adminCanMutateCourses() throws Exception {
        when(courseService.createCourse(any(CourseCreateRequest.class), eq(99L))).thenReturn(course(2L));

        mockMvc.perform(post("/api/v1/courses")
                        .header(HttpHeaders.AUTHORIZATION, bearer("admin@skillora.fr", 99L, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated());

        verify(courseService).createCourse(any(CourseCreateRequest.class), eq(99L));
    }

    @Test
    void professorCanMutateLessonsAndResources() throws Exception {
        mockMvc.perform(delete("/api/v1/lessons/3")
                        .header(HttpHeaders.AUTHORIZATION, bearer("prof@skillora.fr", 42L, "PROFESSOR")))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/resources/5")
                        .header(HttpHeaders.AUTHORIZATION, bearer("prof@skillora.fr", 42L, "PROFESSOR")))
                .andExpect(status().isNoContent());

        verify(lessonService).deleteLesson(3L, 42L, "PROFESSOR");
        verify(resourceService).deleteResource(5L, 42L, "PROFESSOR");
    }

    @Test
    void studentCannotMutateLessonsOrResources() throws Exception {
        mockMvc.perform(delete("/api/v1/lessons/3")
                        .header(HttpHeaders.AUTHORIZATION, bearer("etudiant@skillora.fr", 7L, "STUDENT")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/resources/5")
                        .header(HttpHeaders.AUTHORIZATION, bearer("etudiant@skillora.fr", 7L, "STUDENT")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(lessonService, resourceService);
    }

    @Test
    void internalEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/internal/courses/1"))
                .andExpect(status().isUnauthorized());

        when(courseService.getCourseById(1L, 7L, "STUDENT")).thenReturn(course(1L));
        mockMvc.perform(get("/api/v1/internal/courses/1")
                        .header(HttpHeaders.AUTHORIZATION, bearer("etudiant@skillora.fr", 7L, "STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void corsPreflightFromFrontendIsAllowed() throws Exception {
        mockMvc.perform(options("/api/v1/courses")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
    }

    private String bearer(String email, long userId, String role) {
        return "Bearer " + jwtService.generateToken(email, userId, role);
    }

    private CourseCreateRequest createRequest() {
        return new CourseCreateRequest("Java", "Description", "IT", "DEBUTANT",
                CourseStatus.DRAFT, BigDecimal.TEN);
    }

    private CourseResponse course(Long id) {
        return new CourseResponse(id, "Java", "Description", "IT", "DEBUTANT",
                CourseStatus.DRAFT, BigDecimal.TEN, 42L, null, null, List.of(), List.of());
    }
}
