package com.newsplatform.rbac.service;

import com.newsplatform.auth.service.AuthService;
import com.newsplatform.common.error.DuplicateEmailException;
import com.newsplatform.common.error.RbacException;
import com.newsplatform.rbac.dto.StaffCreateRequest;
import com.newsplatform.rbac.dto.StaffResponse;
import com.newsplatform.rbac.dto.StaffRolesRequest;
import com.newsplatform.rbac.dto.StaffSetupRequest;
import com.newsplatform.rbac.dto.StaffUpdateRequest;
import com.newsplatform.rbac.entity.RoleEntity;
import com.newsplatform.rbac.entity.RoleStatus;
import com.newsplatform.rbac.entity.StaffSetupToken;
import com.newsplatform.rbac.repository.RoleRepository;
import com.newsplatform.rbac.repository.StaffSetupTokenRepository;
import com.newsplatform.user.entity.User;
import com.newsplatform.user.entity.UserStatus;
import com.newsplatform.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StaffService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StaffSetupTokenRepository setupTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final String frontendUrl;
    private final SecureRandom secureRandom = new SecureRandom();

    public StaffService(UserRepository userRepository, RoleRepository roleRepository,
                        StaffSetupTokenRepository setupTokenRepository, PasswordEncoder passwordEncoder,
                        AuditService auditService, @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.setupTokenRepository = setupTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.frontendUrl = frontendUrl;
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> list(String search) {
        List<User> users = search == null || search.isBlank()
                ? userRepository.findAllByOrderByNameAsc()
                : userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByNameAsc(search.trim(), search.trim());
        return users.stream().map(StaffResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public StaffResponse get(UUID id) { return StaffResponse.from(requireUser(id)); }

    @Transactional
    public StaffResponse create(StaffCreateRequest request, UUID actorId) {
        String email = AuthService.normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) throw new DuplicateEmailException();
        Set<RoleEntity> roles = resolveRoles(request.roleIds());
        User user = new User(request.name().trim(), email, passwordEncoder.encode(randomPlaceholder()), UserStatus.PENDING_SETUP);
        user.replaceRoles(roles);
        userRepository.save(user);

        String rawToken = randomToken();
        setupTokenRepository.save(new StaffSetupToken(user, hash(rawToken), Instant.now().plus(24, ChronoUnit.HOURS)));
        auditService.record(actorId, "STAFF_CREATED", "USER", user.getId(), Map.of("email", email));
        return StaffResponse.from(user, frontendUrl.replaceAll("/$", "") + "/staff/setup?token=" + rawToken);
    }

    @Transactional
    public StaffResponse update(UUID id, StaffUpdateRequest request, UUID actorId) {
        User user = requireUser(id);
        String email = AuthService.normalizeEmail(request.email());
        userRepository.findByEmail(email).filter(existing -> !existing.getId().equals(id)).ifPresent(existing -> { throw new DuplicateEmailException(); });
        user.updateProfile(request.name().trim(), email);
        auditService.record(actorId, "STAFF_UPDATED", "USER", id, Map.of("email", email));
        return StaffResponse.from(user);
    }

    @Transactional
    public StaffResponse disable(UUID id, UUID actorId) {
        User user = requireUser(id);
        if (user.getStatus() == UserStatus.ACTIVE && isSuperAdmin(user) && activeSuperAdminCount() <= 1) {
            throw protectedAdmin();
        }
        user.setStatus(UserStatus.DISABLED);
        auditService.record(actorId, "STAFF_DISABLED", "USER", id, Map.of());
        return StaffResponse.from(user);
    }

    @Transactional
    public StaffResponse enable(UUID id, UUID actorId) {
        User user = requireUser(id);
        user.setStatus(UserStatus.ACTIVE);
        auditService.record(actorId, "STAFF_ENABLED", "USER", id, Map.of());
        return StaffResponse.from(user);
    }

    @Transactional
    public StaffResponse updateRoles(UUID id, StaffRolesRequest request, UUID actorId) {
        User user = requireUser(id);
        Set<RoleEntity> roles = resolveRoles(request.roleIds());
        boolean currentlySuperAdmin = isSuperAdmin(user);
        boolean remainsSuperAdmin = roles.stream().anyMatch(role -> role.getCode().equals("SUPER_ADMIN"));
        if (user.getStatus() == UserStatus.ACTIVE && currentlySuperAdmin && !remainsSuperAdmin && activeSuperAdminCount() <= 1) {
            throw protectedAdmin();
        }
        user.replaceRoles(roles);
        auditService.record(actorId, "STAFF_ROLES_CHANGED", "USER", id,
                Map.of("roleIds", roles.stream().map(RoleEntity::getId).map(UUID::toString).toList()));
        return StaffResponse.from(user);
    }

    @Transactional
    public void completeSetup(StaffSetupRequest request) {
        StaffSetupToken setupToken = setupTokenRepository.findByTokenHash(hash(request.token()))
                .orElseThrow(() -> new RbacException(HttpStatus.BAD_REQUEST, "INVALID_SETUP_TOKEN", "This setup link is invalid or expired"));
        if (!setupToken.isUsable(Instant.now()) || setupToken.getUser().getStatus() != UserStatus.PENDING_SETUP) {
            throw new RbacException(HttpStatus.BAD_REQUEST, "INVALID_SETUP_TOKEN", "This setup link is invalid or expired");
        }
        User user = userRepository.findWithRolesById(setupToken.getUser().getId())
                .orElseThrow(() -> new RbacException(HttpStatus.NOT_FOUND, "STAFF_NOT_FOUND", "Staff user not found"));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.ACTIVE);
        setupToken.markUsed();
    }

    @Transactional(readOnly = true)
    public StaffResponse require(UUID id) { return get(id); }

    private User requireUser(UUID id) {
        return userRepository.findWithRolesById(id)
                .orElseThrow(() -> new RbacException(HttpStatus.NOT_FOUND, "STAFF_NOT_FOUND", "Staff user not found"));
    }

    private Set<RoleEntity> resolveRoles(Set<UUID> ids) {
        List<RoleEntity> roles = roleRepository.findAllByIdInAndStatus(ids, RoleStatus.ACTIVE);
        if (roles.size() != ids.size()) throw new RbacException(HttpStatus.BAD_REQUEST, "INVALID_ROLE", "One or more roles do not exist or are inactive");
        return new HashSet<>(roles);
    }

    private boolean isSuperAdmin(User user) { return user.getRoles().stream().anyMatch(role -> role.getCode().equals("SUPER_ADMIN")); }
    private long activeSuperAdminCount() { return userRepository.countByStatusAndRoles_Code(UserStatus.ACTIVE, "SUPER_ADMIN"); }
    private RbacException protectedAdmin() { return new RbacException(HttpStatus.CONFLICT, "LAST_SUPER_ADMIN", "The last active SUPER_ADMIN cannot be disabled or lose SUPER_ADMIN access"); }
    private String randomPlaceholder() { return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes(32)); }
    private String randomToken() { return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes(32)); }
    private byte[] randomBytes(int size) { byte[] bytes = new byte[size]; secureRandom.nextBytes(bytes); return bytes; }
    private String hash(String raw) {
        try { return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException exception) { throw new IllegalStateException("SHA-256 is unavailable", exception); }
    }
}
