package com.elearning.complaint.controller;

import com.elearning.complaint.dto.CategoryResponse;
import com.elearning.complaint.dto.StatusResponse;
import com.elearning.complaint.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Référentiels", description = "Catégories et statuts des réclamations")
@SecurityRequirement(name = "bearerAuth")
public class ReferenceController {

    private final ComplaintService complaintService;

    public ReferenceController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping("/complaint-categories")
    @Operation(summary = "Lister toutes les catégories de réclamations",
               description = "Retourne la liste de toutes les catégories disponibles. Nécessite un JWT valide.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des catégories"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(complaintService.getAllCategories());
    }

    @GetMapping("/complaint-statuses")
    @Operation(summary = "Lister tous les statuts de réclamations",
               description = "Retourne la liste de tous les statuts disponibles. Nécessite un JWT valide.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des statuts"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<List<StatusResponse>> getStatuses() {
        return ResponseEntity.ok(complaintService.getAllStatuses());
    }
}
