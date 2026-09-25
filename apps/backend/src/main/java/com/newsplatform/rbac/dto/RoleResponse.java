package com.newsplatform.rbac.dto;

import com.newsplatform.rbac.entity.RoleEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RoleResponse(UUID id, String name, String code, String description, boolean systemRole,
                           String status, List<String> permissionCodes, Instant createdAt, Instant updatedAt) {
    public static RoleResponse from(RoleEntity role) {
        return new RoleResponse(role.getId(), role.getName(), role.getCode(), role.getDescription(), role.isSystemRole(),
                role.getStatus().name(), role.getPermissions().stream().map(permission -> permission.getCode()).sorted().toList(),
                role.getCreatedAt(), role.getUpdatedAt());
    }
}
