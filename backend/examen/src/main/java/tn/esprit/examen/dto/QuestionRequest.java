package tn.esprit.examen.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class QuestionRequest {
    @NotBlank private String enonce;
    @NotBlank private String typeQuestion;
    @NotBlank private String optionA;
    @NotBlank private String optionB;
    private String optionC;
    private String optionD;
    @NotBlank @Pattern(regexp = "[ABCD]", message = "bonneReponse doit être A, B, C ou D")
    private String bonneReponse;
    @Positive private double points;
}