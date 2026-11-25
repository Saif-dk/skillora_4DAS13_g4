package tn.esprit.examen.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamenRequest {
    @NotBlank private String titre;
    private String description;
    @NotNull @Future private LocalDateTime dateExamen;
    @Positive private int duree;
    @NotBlank private String courseId;
    @NotBlank private String teacherId;
}