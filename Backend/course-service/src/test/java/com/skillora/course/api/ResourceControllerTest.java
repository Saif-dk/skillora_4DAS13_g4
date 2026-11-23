package com.skillora.course.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillora.course.api.dto.ResourceCreateRequest;
import com.skillora.course.api.dto.ResourceResponse;
import com.skillora.course.domain.enumeration.ResourceType;
import com.skillora.course.security.JwtService;
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ResourceService resourceService;

    @Test
    void createCourseResourceReturns201AndOverridesBodyIds() throws Exception {
        ResourceCreateRequest body = bodyWithSpoofedIds();
        ResourceCreateRequest attached = new ResourceCreateRequest(
                10L, null, body.name(), body.type(), body.url(), body.size());
        when(resourceService.createResource(eq(attached), eq(42L), eq("PROFESSOR")))
                .thenReturn(resource(5L, 10L, null));

        mockMvc.perform(post("/api/v1/courses/10/resources")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.courseId").value(10));

        verify(resourceService).createResource(eq(attached), eq(42L), eq("PROFESSOR"));
    }

    @Test
    void createLessonResourceReturns201AndOverridesBodyIds() throws Exception {
        ResourceCreateRequest body = bodyWithSpoofedIds();
        ResourceCreateRequest attached = new ResourceCreateRequest(
                null, 20L, body.name(), body.type(), body.url(), body.size());
        when(resourceService.createResource(eq(attached), eq(42L), eq("PROFESSOR")))
                .thenReturn(resource(6L, null, 20L));

        mockMvc.perform(post("/api/v1/lessons/20/resources")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.lessonId").value(20));

        verify(resourceService).createResource(eq(attached), eq(42L), eq("PROFESSOR"));
    }

    @Test
    void getResourceByIdReturns200() throws Exception {
        when(resourceService.getResourceById(5L, 42L, "PROFESSOR")).thenReturn(resource(5L, 10L, null));

        mockMvc.perform(get("/api/v1/resources/5")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Support"));
    }

    @Test
    void deleteResourceReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/resources/5")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR")))
                .andExpect(status().isNoContent());

        verify(resourceService).deleteResource(5L, 42L, "PROFESSOR");
    }

    @Test
    void invalidBodyReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/courses/10/resources")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"type\":\"PDF\",\"url\":\"https://exemple.fr/pdf\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("name")));

        verify(resourceService, never()).createResource(any(), any(), any());
    }

    @Test
    void invalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/resources/5")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token.invalide"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(resourceService);
    }

    @Test
    void studentCannotCreateResource() throws Exception {
        mockMvc.perform(post("/api/v1/courses/10/resources")
                        .header(HttpHeaders.AUTHORIZATION, bearer("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bodyWithSpoofedIds())))
                .andExpect(status().isForbidden());

        verifyNoInteractions(resourceService);
    }

    private ResourceCreateRequest bodyWithSpoofedIds() {
        return new ResourceCreateRequest(999L, 999L, "Support", ResourceType.PDF,
                "https://exemple.fr/pdf", "2MB");
    }

    private String bearer(String role) {
        return "Bearer " + jwtService.generateToken("enseignant@skillora.fr", 42L, role);
    }

    private ResourceResponse resource(Long id, Long courseId, Long lessonId) {
        return new ResourceResponse(id, courseId, lessonId, "Support", ResourceType.PDF,
                "https://exemple.fr/pdf", "2MB", null);
    }
}
