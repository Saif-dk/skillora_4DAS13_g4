package com.skillora.course.security;

public record AuthenticatedUser(Long userId, String email, String role) {
}
