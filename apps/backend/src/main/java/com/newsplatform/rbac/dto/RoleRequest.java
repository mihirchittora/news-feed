package com.newsplatform.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RoleRequest(
        @NotBlank(message = "Role name is required")
        @Size(max = 120, message = "Role name must be 120 characters or fewer")
        String name,
        @NotBlank(message = "Role code is required")
        @Pattern(regexp = "[A-Z][A-Z0-9_]*", message = "Role code must use uppercase letters, numbers, and underscores")
        @Size(max = 80, message = "Role code must be 80 characters or fewer")
        String code,
        @Size(max = 500, message = "Description must be 500 characters or fewer")
        String description,
        Set<String> permissionCodes,
        String status
) {
}
