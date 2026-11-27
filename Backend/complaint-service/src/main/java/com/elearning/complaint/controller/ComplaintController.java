package com.elearning.complaint.controller;

import com.elearning.complaint.dto.*;
import com.elearning.complaint.security.AuthUser;
import com.elearning.complaint.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/complaints")
@Tag(name = "Réclamations", description = "Gestion des réclamations (STUDENT / TEACHER)")
@SecurityRequirement(name = "bearerAuth")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping
    @Operation(summary = "Créer une réclamation (STUDENT ou TEACHER)",
               description = "Soumet une nouvelle réclamation. Le requesterId est extrait du JWT, jamais du body. Statut initial : OPEN. Interdit aux ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Réclamation créée"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Interdit aux ADMIN"),
            @ApiResponse(responseCode = "404", description = "Catégorie introuvable")
    })
    public ResponseEntity<ComplaintResponse> createComplaint(
            @Valid @RequestBody CreateComplaintRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(complaintService.createComplaint(request, authUser));
    }

    @GetMapping
    @Operation(summary = "Lister les réclamations",
               description = "STUDENT/TEACHER : uniquement ses propres réclamations. ADMIN : toutes, avec filtre optionnel ?status=OPEN|IN_PROGRESS|ACCEPTED|REJECTED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des réclamations"),
            @ApiResponse(responseCode = "400", description = "Statut de filtre inconnu"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<List<ComplaintResponse>> getComplaints(
            @RequestParam(required = false) String status,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(complaintService.getComplaints(authUser, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une réclamation par ID (avec ses réponses)",
               description = "STUDENT/TEACHER : uniquement sa propre réclamation. ADMIN : toutes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réclamation trouvée"),
            @ApiResponse(responseCode = "400", description = "ID invalide (pas un UUID)"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Réclamation appartenant à un autre utilisateur"),
            @ApiResponse(responseCode = "404", description = "Réclamation introuvable")
    })
    public ResponseEntity<ComplaintResponse> getComplaintById(
            @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(complaintService.getComplaintById(id.toString(), authUser));
    }

    @PostMapping("/{id}/replies")
    @Operation(summary = "Ajouter une réponse à une réclamation",
               description = "Autorisé : propriétaire de la réclamation ou ADMIN. Si ADMIN répond et statut OPEN → passage automatique à IN_PROGRESS. Interdit si statut ACCEPTED ou REJECTED.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Réponse ajoutée"),
            @ApiResponse(responseCode = "400", description = "Contenu vide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Non autorisé"),
            @ApiResponse(responseCode = "404", description = "Réclamation introuvable"),
            @ApiResponse(responseCode = "409", description = "Réclamation déjà ACCEPTED ou REJECTED")
    })
    public ResponseEntity<ReplyResponse> addReply(
            @PathVariable UUID id,
            @Valid @RequestBody ReplyRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(complaintService.addReply(id.toString(), request, authUser));
    }
}
