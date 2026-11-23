package com.skillora.course.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillora.course.api.dto.CourseCreateRequest;
import com.skillora.course.api.dto.CourseResponse;
import com.skillora.course.api.dto.CourseUpdateRequest;
import com.skillora.course.api.dto.PageResponse;
import com.skillora.course.domain.enumeration.CourseStatus;
import com.skillora.course.security.JwtService;
import com.skillora.course.service.CourseFilter;
import com.skillora.course.service.CourseService;
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourseService courseService;

    @Test
    void createCourseReturns201AndUsesJwtUserIdAsTeacherId() throws Exception {
        CourseCreateRequest request = new CourseCreateRequest(
                "Java", "Description", "IT", "DEBUTANT", CourseStatus.DRAFT, BigDecimal.TEN);
        when(courseService.createCourse(any(CourseCreateRequest.class), eq(42L))).thenReturn(course(1L));

        mockMvc.perform(post("/api/v1/courses")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.teacherId").value(42));

        verify(courseService).createCourse(eq(request), eq(42L));
    }

    @Test
    void listCoursesReturns200WithFilters() throws Exception {
        CourseFilter filter = CourseFilter.of("java", CourseStatus.PUBLISHED, 2, 5);
        when(courseService.listCourses(eq(filter), eq(42L), eq("PROFESSOR")))
                .thenReturn(new PageResponse<>(List.of(), 2, 5, 0, 0));

        mockMvc.perform(get("/api/v1/courses")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR"))
                        .param("search", "java")
                        .param("status", "PUBLISHED")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(5));

        verify(courseService).listCourses(eq(filter), eq(42L), eq("PROFESSOR"));
    }

    @Test
    void getCourseByIdReturns200() throws Exception {
        when(courseService.getCourseById(1L, 42L, "PROFESSOR")).thenReturn(course(1L));

        mockMvc.perform(get("/api/v1/courses/1")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java"));
    }

    @Test
    void updateCourseReturns200() throws Exception {
        CourseUpdateRequest request = new CourseUpdateRequest(
                "Java avance", "Description", "IT", "AVANCE", CourseStatus.DRAFT, BigDecimal.TEN);
        when(courseService.updateCourse(eq(1L), eq(request), eq(42L), eq("PROFESSOR"))).thenReturn(course(1L));

        mockMvc.perform(put("/api/v1/courses/1")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void publishAndUnpublishReturn200() throws Exception {
        when(courseService.publishCourse(1L, 42L, "PROFESSOR")).thenReturn(course(1L));
        when(courseService.unpublishCourse(1L, 42L, "PROFESSOR")).thenReturn(course(1L));

        mockMvc.perform(patch("/api/v1/courses/1/publish")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR")))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/v1/courses/1/unpublish")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR")))
                .andExpect(status().isOk());

        verify(courseService).publishCourse(1L, 42L, "PROFESSOR");
        verify(courseService).unpublishCourse(1L, 42L, "PROFESSOR");
    }

    @Test
    void deleteCourseReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/courses/1")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR")))
                .andExpect(status().isNoContent());

        verify(courseService).deleteCourse(1L, 42L, "PROFESSOR");
    }

    @Test
    void invalidBodyReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/courses")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("title")));

        verify(courseService, never()).createCourse(any(), any());
    }

    @Test
    void invalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/courses/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token.invalide"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(courseService);
    }

    @Test
    void studentCannotCreateCourse() throws Exception {
        mockMvc.perform(post("/api/v1/courses")
                        .header(HttpHeaders.AUTHORIZATION, bearer("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CourseCreateRequest(
                                "Java", null, null, null, CourseStatus.DRAFT, BigDecimal.ZERO))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(courseService);
    }

    private String bearer(String role) {
        return "Bearer " + jwtService.generateToken("enseignant@skillora.fr", 42L, role);
    }

    private CourseResponse course(Long id) {
        return new CourseResponse(id, "Java", "Description", "IT", "DEBUTANT",
                CourseStatus.DRAFT, BigDecimal.TEN, 42L, null, null, List.of(), List.of());
    }
}
