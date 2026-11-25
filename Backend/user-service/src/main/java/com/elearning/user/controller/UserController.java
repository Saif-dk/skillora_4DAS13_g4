package com.elearning.user.controller;

import com.elearning.user.dto.UserResponse;
import com.elearning.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    /**
     * GET /api/v1/users/me
     * JWT required — returns the profile of the currently authenticated user.
     * The principal is the userId (String) stored in the JWT subject.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(Authentication authentication) {
        // JwtAuthFilter sets the principal = userId
        String userId = (String) authentication.getPrincipal();
        UserResponse user = authService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
}
