package com.newsplatform.rbac.dto;

import com.newsplatform.rbac.entity.RoleEntity;

import java.util.UUID;

public record RoleSummary(UUID id, String code, String name) {
    public static RoleSummary from(RoleEntity role) {
        return new RoleSummary(role.getId(), role.getCode(), role.getName());
    }
}
