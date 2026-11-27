package com.elearning.complaint.security;

/**
 * Represents an authenticated user extracted from a JWT token.
 * subject = userId, role = STUDENT | TEACHER | ADMIN
 */
public record AuthUser(String userId, String email, String role) {
}
