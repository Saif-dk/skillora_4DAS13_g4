package com.elearning.user.config;

import com.elearning.user.model.Role;
import com.elearning.user.model.User;
import com.elearning.user.repository.RoleRepository;
import com.elearning.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Runs once at startup to seed the database with default roles and an admin user.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository  roleRepository;
    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedRoles();
        seedAdminUser();
    }

    // ── Roles ────────────────────────────────────────────────────────────────────
    private void seedRoles() {
        List<RoleSeed> seeds = List.of(
                new RoleSeed("STUDENT", "Platform learner"),
                new RoleSeed("TEACHER", "Course instructor"),
                new RoleSeed("ADMIN",   "Platform administrator")
        );

        for (RoleSeed seed : seeds) {
            if (!roleRepository.existsByName(seed.name())) {
                Role role = Role.builder()
                        .name(seed.name())
                        .description(seed.description())
                        .build();
                roleRepository.save(role);
                log.info("[DataInitializer] Created role: {}", seed.name());
            } else {
                log.debug("[DataInitializer] Role already exists: {}", seed.name());
            }
        }
    }

    // ── Admin user ───────────────────────────────────────────────────────────────
    private void seedAdminUser() {
        final String adminEmail = "admin@elearning.com";

        if (userRepository.existsByEmail(adminEmail)) {
            log.debug("[DataInitializer] Admin user already exists, skipping.");
            return;
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found — check seedRoles()"));

        User admin = User.builder()
                .firstName("Admin")
                .lastName("ESPRIT")
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode("Admin123!"))
                .roleId(adminRole.getId())
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        userRepository.save(admin);
        log.info("[DataInitializer] Admin user created: {}", adminEmail);
    }

    // ── Inner record ─────────────────────────────────────────────────────────────
    private record RoleSeed(String name, String description) {}
}
