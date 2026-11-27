package com.elearning.user.controller;

import com.elearning.user.dto.UserResponse;
import com.elearning.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Endpoints d'administration (réservés au rôle ADMIN)")
public class AdminUserController {

    private final AuthService authService;

    public AdminUserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/users")
    @Operation(summary = "Lister tous les utilisateurs de la plateforme (ADMIN uniquement)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des utilisateurs retournée"),
            @ApiResponse(responseCode = "401", description = "Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé : rôle ADMIN requis")
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
