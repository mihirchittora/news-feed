package com.newsplatform.rbac.service;

import com.newsplatform.common.error.RbacException;
import com.newsplatform.rbac.dto.PermissionCodesRequest;
import com.newsplatform.rbac.dto.PermissionGroupResponse;
import com.newsplatform.rbac.dto.PermissionResponse;
import com.newsplatform.rbac.dto.RoleRequest;
import com.newsplatform.rbac.dto.RoleResponse;
import com.newsplatform.rbac.entity.Permission;
import com.newsplatform.rbac.entity.RoleEntity;
import com.newsplatform.rbac.entity.RoleStatus;
import com.newsplatform.rbac.repository.PermissionRepository;
import com.newsplatform.rbac.repository.RoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditService auditService;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository, AuditService auditService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        return roleRepository.findAllByOrderBySystemRoleDescNameAsc().stream().map(RoleResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse getRole(UUID id) {
        return RoleResponse.from(requireRole(id));
    }

    @Transactional(readOnly = true)
    public List<PermissionGroupResponse> listPermissions() {
        Map<String, List<PermissionResponse>> groups = permissionRepository.findAllByOrderByCategoryAscCodeAsc().stream()
                .map(PermissionResponse::from)
                .collect(Collectors.groupingBy(PermissionResponse::category, java.util.LinkedHashMap::new, Collectors.toList()));
        return groups.entrySet().stream().map(entry -> new PermissionGroupResponse(entry.getKey(), entry.getValue())).toList();
    }

    @Transactional
    public RoleResponse create(RoleRequest request, UUID actorId) {
        String name = request.name().trim();
        String code = normalizeCode(request.code());
        if (roleRepository.existsByCode(code)) throw conflict("DUPLICATE_ROLE_CODE", "A role with this code already exists");
        if (roleRepository.existsByNameIgnoreCase(name)) throw conflict("DUPLICATE_ROLE_NAME", "A role with this name already exists");
        RoleEntity role = new RoleEntity(name, code, trim(request.description()));
        role.replacePermissions(resolvePermissions(request.permissionCodes()));
        roleRepository.save(role);
        auditService.record(actorId, "ROLE_CREATED", "ROLE", role.getId(), Map.of("code", code, "name", name));
        return RoleResponse.from(role);
    }

    @Transactional
    public RoleResponse update(UUID id, RoleRequest request, UUID actorId) {
        RoleEntity role = requireRole(id);
        ensureCustom(role);
        String name = request.name().trim();
        String requestedCode = normalizeCode(request.code());
        if (!role.getCode().equals(requestedCode)) throw conflict("ROLE_CODE_IMMUTABLE", "Role codes cannot be changed after creation");
        if (!role.getName().equalsIgnoreCase(name) && roleRepository.existsByNameIgnoreCase(name)) {
            throw conflict("DUPLICATE_ROLE_NAME", "A role with this name already exists");
        }
        role.update(name, trim(request.description()), parseStatus(request.status()));
        auditService.record(actorId, "ROLE_UPDATED", "ROLE", role.getId(), Map.of("code", role.getCode()));
        return RoleResponse.from(role);
    }

    @Transactional
    public void updatePermissions(UUID id, PermissionCodesRequest request, UUID actorId) {
        RoleEntity role = requireRole(id);
        ensureCustom(role);
        role.replacePermissions(resolvePermissions(request.permissionCodes()));
        auditService.record(actorId, "ROLE_PERMISSIONS_CHANGED", "ROLE", role.getId(), Map.of("permissionCodes", request.permissionCodes()));
    }

    @Transactional
    public void delete(UUID id, UUID actorId) {
        RoleEntity role = requireRole(id);
        ensureCustom(role);
        if (!role.getUsers().isEmpty()) throw conflict("ROLE_ASSIGNED", "Reassign or remove this role from staff before deleting it");
        roleRepository.delete(role);
        auditService.record(actorId, "ROLE_DELETED", "ROLE", id, Map.of("code", role.getCode()));
    }

    public RoleEntity requireRole(UUID id) {
        return roleRepository.findWithPermissionsById(id)
                .orElseThrow(() -> new RbacException(HttpStatus.NOT_FOUND, "ROLE_NOT_FOUND", "Role not found"));
    }

    private Set<Permission> resolvePermissions(Set<String> requestedCodes) {
        Set<String> codes = requestedCodes == null ? Set.of() : requestedCodes.stream()
                .map(this::normalizeCode).collect(Collectors.toSet());
        List<Permission> permissions = permissionRepository.findAllByCodeIn(codes);
        if (permissions.size() != codes.size()) {
            Set<String> found = permissions.stream().map(Permission::getCode).collect(Collectors.toSet());
            Set<String> missing = new HashSet<>(codes);
            missing.removeAll(found);
            throw new RbacException(HttpStatus.BAD_REQUEST, "INVALID_PERMISSION", "Unknown permission code(s): " + String.join(", ", missing));
        }
        return new HashSet<>(permissions);
    }

    private void ensureCustom(RoleEntity role) {
        if (role.isSystemRole()) throw conflict("SYSTEM_ROLE_PROTECTED", "System roles are protected");
    }

    private RoleStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return RoleStatus.ACTIVE;
        try { return RoleStatus.valueOf(status.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException exception) { throw new RbacException(HttpStatus.BAD_REQUEST, "INVALID_ROLE_STATUS", "Role status must be ACTIVE or INACTIVE"); }
    }

    private String normalizeCode(String code) { return code == null ? "" : code.trim().toUpperCase(Locale.ROOT); }
    private String trim(String value) { return value == null ? null : value.trim(); }
    private RbacException conflict(String code, String message) { return new RbacException(HttpStatus.CONFLICT, code, message); }
}
