package com.newsplatform.rbac.dto;

import com.newsplatform.rbac.entity.Permission;

public record PermissionResponse(String code, String name, String description, String category) {
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(permission.getCode(), permission.getName(), permission.getDescription(), permission.getCategory());
    }
}
