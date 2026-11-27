package com.elearning.complaint.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ReplyRequest(

        @NotBlank(message = "Le contenu ne peut pas être vide")
        @Schema(example = "Merci de me répondre rapidement.")
        String content
) {
}
