package com.skillora.course.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillora.course.api.dto.LessonCreateRequest;
import com.skillora.course.api.dto.LessonResponse;
import com.skillora.course.api.dto.LessonUpdateRequest;
import com.skillora.course.security.JwtService;
import com.skillora.course.service.LessonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LessonService lessonService;

    @Test
    void listLessonsReturns200() throws Exception {
        when(lessonService.listLessons(1L, 42L, "PROFESSOR")).thenReturn(List.of(lesson(3L)));

        mockMvc.perform(get("/api/v1/courses/1/lessons")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    void addLessonReturns201() throws Exception {
        LessonCreateRequest request = new LessonCreateRequest("Lecon 1", "Contenu", 1);
        when(lessonService.addLesson(eq(1L), eq(request), eq(42L), eq("PROFESSOR"))).thenReturn(lesson(3L));

        mockMvc.perform(post("/api/v1/courses/1/lessons")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3));

        verify(lessonService).addLesson(1L, request, 42L, "PROFESSOR");
    }

    @Test
    void updateLessonReturns200() throws Exception {
        LessonUpdateRequest request = new LessonUpdateRequest("Lecon modifiee", "Contenu", 2);
        when(lessonService.updateLesson(eq(3L), eq(request), eq(42L), eq("PROFESSOR"))).thenReturn(lesson(3L));

        mockMvc.perform(put("/api/v1/lessons/3")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void reorderLessonReturns200() throws Exception {
        when(lessonService.reorderLesson(3L, 4, 42L, "PROFESSOR")).thenReturn(lesson(3L));

        mockMvc.perform(patch("/api/v1/lessons/3/reorder")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR"))
                        .param("position", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value(1));
    }

    @Test
    void deleteLessonReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/lessons/3")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR")))
                .andExpect(status().isNoContent());

        verify(lessonService).deleteLesson(3L, 42L, "PROFESSOR");
    }

    @Test
    void invalidBodyReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/courses/1/lessons")
                        .header(HttpHeaders.AUTHORIZATION, bearer("PROFESSOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"position\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("title")));

        verify(lessonService, never()).addLesson(any(), any(), any(), any());
    }

    @Test
    void missingTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/courses/1/lessons"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(lessonService);
    }

    @Test
    void studentCannotAddLesson() throws Exception {
        mockMvc.perform(post("/api/v1/courses/1/lessons")
                        .header(HttpHeaders.AUTHORIZATION, bearer("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LessonCreateRequest("Lecon", null, 1))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(lessonService);
    }

    private String bearer(String role) {
        return "Bearer " + jwtService.generateToken("enseignant@skillora.fr", 42L, role);
    }

    private LessonResponse lesson(Long id) {
        return new LessonResponse(id, 1L, "Lecon", "Contenu", 1, null, null, List.of());
    }
}
