package com.elearning.user.controller;

import com.elearning.user.dto.UserResponse;
import com.elearning.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Endpoints de gestion du profil utilisateur authentifié")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    @Operation(summary = "Obtenir le profil de l'utilisateur connecté (token JWT requis)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil retourné avec succès"),
            @ApiResponse(responseCode = "401", description = "Token JWT manquant ou invalide")
    })
    public ResponseEntity<UserResponse> getMe(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        UserResponse response = authService.getUserById(userId);
        return ResponseEntity.ok(response);
    }
}
