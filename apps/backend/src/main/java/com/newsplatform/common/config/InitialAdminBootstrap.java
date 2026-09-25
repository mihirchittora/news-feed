package com.newsplatform.common.config;

import com.newsplatform.auth.service.AuthService;
import com.newsplatform.rbac.entity.RoleEntity;
import com.newsplatform.rbac.repository.RoleRepository;
import com.newsplatform.user.entity.User;
import com.newsplatform.user.entity.UserStatus;
import com.newsplatform.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InitialAdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialAdminBootstrap.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String name;

    public InitialAdminBootstrap(
            UserRepository userRepository,
            RoleRepository roleRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
            @Value("${app.initial-admin.email:}") String email,
            @Value("${app.initial-admin.password:}") String password,
            @Value("${app.initial-admin.name:}") String name
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            log.info("Initial admin bootstrap skipped: all INITIAL_ADMIN_* values are not configured");
            return;
        }

        String normalizedEmail = AuthService.normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            log.info("Initial admin bootstrap skipped: an account already exists for the configured email");
            return;
        }

        RoleEntity superAdmin = roleRepository.findByCode("SUPER_ADMIN")
                .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN system role is not configured"));
        User user = new User(
                name.trim(),
                normalizedEmail,
                passwordEncoder.encode(password),
                UserStatus.ACTIVE
        );
        user.addRole(superAdmin);
        userRepository.save(user);
        log.info("Initial admin account created for the configured email");
    }
}
