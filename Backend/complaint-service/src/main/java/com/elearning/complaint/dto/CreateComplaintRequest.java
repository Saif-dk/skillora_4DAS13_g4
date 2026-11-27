package com.elearning.complaint.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateComplaintRequest(

        @NotBlank(message = "Le sujet ne peut pas être vide")
        @Size(max = 150, message = "Le sujet ne doit pas dépasser 150 caractères")
        @Schema(example = "Problème de paiement")
        String subject,

        @NotBlank(message = "La description ne peut pas être vide")
        @Schema(example = "J'ai payé mais je n'ai pas accès au cours.")
        String description,

        @NotNull(message = "La catégorie est obligatoire")
        @Schema(description = "UUID obtenu via GET /api/v1/complaint-categories")
        UUID categoryId
) {
}
