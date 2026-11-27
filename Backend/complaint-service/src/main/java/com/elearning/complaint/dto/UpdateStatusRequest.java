package com.elearning.complaint.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateStatusRequest(

        @NotBlank(message = "Le statut ne peut pas être vide")
        @Schema(example = "ACCEPTED", description = "Valeurs autorisées : IN_PROGRESS, ACCEPTED, REJECTED")
        String status
) {
}
