package com.newsplatform.auth.service;

import com.newsplatform.auth.dto.LoginRequest;
import com.newsplatform.auth.dto.LoginResponse;
import com.newsplatform.auth.dto.RegisterRequest;
import com.newsplatform.common.error.DuplicateEmailException;
import com.newsplatform.common.error.InvalidCredentialsException;
import com.newsplatform.common.security.JwtService;
import com.newsplatform.user.dto.UserResponse;
import com.newsplatform.user.entity.Role;
import com.newsplatform.user.entity.User;
import com.newsplatform.user.entity.UserStatus;
import com.newsplatform.user.repository.UserRepository;
import com.newsplatform.rbac.entity.RoleEntity;
import com.newsplatform.rbac.repository.RoleRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this(userRepository, passwordEncoder, jwtService, null);
    }

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        User user = roleRepository == null
                ? new User(request.name().trim(), email, passwordEncoder.encode(request.password()), Role.USER, UserStatus.ACTIVE)
                : new User(request.name().trim(), email, passwordEncoder.encode(request.password()), UserStatus.ACTIVE);
        if (roleRepository != null) {
            RoleEntity userRole = roleRepository.findByCode("USER")
                    .orElseThrow(() -> new IllegalStateException("USER system role is not configured"));
            user.addRole(userRole);
        }

        try {
            return UserResponse.from(userRepository.save(user));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null
                || user.getStatus() != UserStatus.ACTIVE
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new LoginResponse(
                jwtService.generateToken(user),
                "Bearer",
                jwtService.getExpirationSeconds(),
                UserResponse.from(user)
        );
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
