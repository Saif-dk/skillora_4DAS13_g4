package com.elearning.forum.security;

/**
 * Authenticated user record extracted from JWT claims.
 * Retrieved in controllers via @AuthenticationPrincipal.
 */
public record AuthUser(
        String userId,
        String email,
        String role
) {
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
