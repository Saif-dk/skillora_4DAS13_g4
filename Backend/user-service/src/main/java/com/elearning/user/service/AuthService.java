package com.elearning.user.service;

import com.elearning.user.dto.AuthResponse;
import com.elearning.user.dto.LoginRequest;
import com.elearning.user.dto.RegisterRequest;
import com.elearning.user.dto.UserResponse;
import com.elearning.user.exception.EmailAlreadyUsedException;
import com.elearning.user.exception.InvalidCredentialsException;
import com.elearning.user.exception.ResourceNotFoundException;
import com.elearning.user.exception.RoleNotFoundException;
import com.elearning.user.model.Role;
import com.elearning.user.model.User;
import com.elearning.user.repository.RoleRepository;
import com.elearning.user.repository.UserRepository;
import com.elearning.user.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository  userRepository;
    private final RoleRepository  roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService      jwtService;

    // ── Register ──────────────────────────────────────────────────────────────────
    public UserResponse register(RegisterRequest request) {

        // Block ADMIN self-registration
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            throw new IllegalArgumentException("Role ADMIN cannot be assigned during registration");
        }

        // Email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyUsedException(request.getEmail());
        }

        // Resolve role (default: STUDENT)
        String roleName = (request.getRole() == null || request.getRole().isBlank())
                ? "STUDENT" : request.getRole().toUpperCase();

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException(roleName));

        // Persist user
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roleId(role.getId())
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        user = userRepository.save(user);
        log.info("[AuthService] Registered user: {} (role={})", user.getEmail(), roleName);

        return toUserResponse(user, role);
    }

    // ── Login ─────────────────────────────────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", user.getRoleId()));

        String token = jwtService.generateToken(user.getId(), user.getEmail(), role.getName());

        log.info("[AuthService] User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(toUserResponse(user, role))
                .build();
    }

    // ── Get user by ID ─────────────────────────────────────────────────────────────
    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", user.getRoleId()));

        return toUserResponse(user, role);
    }

    // ── List all users (admin) ─────────────────────────────────────────────────────
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    Role role = roleRepository.findById(user.getRoleId()).orElse(null);
                    return toUserResponse(user, role);
                })
                .collect(Collectors.toList());
    }

    // ── Mapper ────────────────────────────────────────────────────────────────────
    private UserResponse toUserResponse(User user, Role role) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(role != null ? role.getName() : "UNKNOWN")
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
