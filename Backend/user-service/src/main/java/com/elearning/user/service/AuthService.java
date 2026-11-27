package com.elearning.user.service;

import com.elearning.user.dto.AuthResponse;
import com.elearning.user.dto.LoginRequest;
import com.elearning.user.dto.RegisterRequest;
import com.elearning.user.dto.UserResponse;
import com.elearning.user.exception.BadRequestException;
import com.elearning.user.exception.EmailAlreadyUsedException;
import com.elearning.user.exception.InvalidCredentialsException;
import com.elearning.user.exception.ResourceNotFoundException;
import com.elearning.user.model.Role;
import com.elearning.user.model.User;
import com.elearning.user.repository.RoleRepository;
import com.elearning.user.repository.UserRepository;
import com.elearning.user.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(RegisterRequest request) {
        // Normalize email
        String email = request.getEmail().trim().toLowerCase();

        // Resolve role
        String roleName = resolveRoleName(request.getRole());

        // Check email uniqueness
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }

        // Find role document
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new BadRequestException("Role not found in database: " + roleName));

        // Build and save user
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roleId(role.getId())
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        User saved = userRepository.save(user);
        return toUserResponse(saved, role.getName());
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String roleName = roleRepository.findById(user.getRoleId())
                .map(Role::getName)
                .orElse("UNKNOWN");

        String token = jwtService.generateToken(user.getId(), user.getEmail(), roleName);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(toUserResponse(user, roleName))
                .build();
    }

    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        String roleName = roleRepository.findById(user.getRoleId())
                .map(Role::getName)
                .orElse("UNKNOWN");

        return toUserResponse(user, roleName);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    String roleName = roleRepository.findById(user.getRoleId())
                            .map(Role::getName)
                            .orElse("UNKNOWN");
                    return toUserResponse(user, roleName);
                })
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String resolveRoleName(String rawRole) {
        if (rawRole == null || rawRole.trim().isEmpty()) {
            return "STUDENT";
        }
        String roleName = rawRole.trim().toUpperCase();
        if ("ADMIN".equals(roleName)) {
            throw new BadRequestException("Cannot register with role ADMIN");
        }
        if (!"STUDENT".equals(roleName) && !"TEACHER".equals(roleName)) {
            throw new BadRequestException("Invalid role: " + roleName + ". Allowed values: STUDENT, TEACHER");
        }
        return roleName;
    }

    private UserResponse toUserResponse(User user, String roleName) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(roleName)
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
