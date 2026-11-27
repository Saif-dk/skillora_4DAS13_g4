package com.elearning.complaint.controller;

import com.elearning.complaint.dto.ComplaintResponse;
import com.elearning.complaint.dto.UpdateStatusRequest;
import com.elearning.complaint.security.AuthUser;
import com.elearning.complaint.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/complaints")
@Tag(name = "Admin - Réclamations", description = "Gestion administrative des réclamations (ADMIN uniquement)")
@SecurityRequirement(name = "bearerAuth")
public class AdminComplaintController {

    private final ComplaintService complaintService;

    public AdminComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'une réclamation (ADMIN)",
               description = "Permet à un administrateur de changer le statut : IN_PROGRESS, ACCEPTED ou REJECTED. OPEN est interdit. Impossible si déjà ACCEPTED ou REJECTED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statut mis à jour"),
            @ApiResponse(responseCode = "400", description = "Statut invalide (y compris OPEN)"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Réservé aux ADMIN"),
            @ApiResponse(responseCode = "404", description = "Réclamation introuvable"),
            @ApiResponse(responseCode = "409", description = "Réclamation déjà ACCEPTED ou REJECTED")
    })
    public ResponseEntity<ComplaintResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(complaintService.updateStatus(id.toString(), request, authUser));
    }
}
