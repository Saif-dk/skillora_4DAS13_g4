package com.elearning.user.config;

import com.elearning.user.model.Role;
import com.elearning.user.model.User;
import com.elearning.user.repository.RoleRepository;
import com.elearning.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create default roles if they don't exist
        createRoleIfNotExists("STUDENT", "Platform student");
        createRoleIfNotExists("TEACHER", "Platform teacher");
        createRoleIfNotExists("ADMIN", "Platform administrator");

        // Create default admin user if not exists
        String adminEmail = "admin@elearning.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new IllegalStateException("ADMIN role not found after initialization"));

            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("Skillora")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("Admin123!"))
                    .roleId(adminRole.getId())
                    .status("ACTIVE")
                    .createdAt(Instant.now())
                    .build();

            userRepository.save(admin);
            log.info("Default admin user created: {}", adminEmail);
        }
    }

    private void createRoleIfNotExists(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(Role.builder()
                    .name(name)
                    .description(description)
                    .build());
            log.info("Role created: {}", name);
        }
    }
}
