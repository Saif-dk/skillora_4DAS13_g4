package com.elearning.user.controller;

import com.elearning.user.dto.UserResponse;
import com.elearning.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final AuthService authService;

    /**
     * GET /api/v1/admin/users
     * ADMIN role only — returns the full list of users (without passwordHash).
     * The URL-level rule in SecurityConfig already blocks non-ADMINs with 403.
     * @PreAuthorize adds a second, method-level guard for extra safety.
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
