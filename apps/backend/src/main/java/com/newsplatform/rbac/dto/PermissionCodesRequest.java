package com.newsplatform.rbac.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record PermissionCodesRequest(@NotNull Set<String> permissionCodes) {
}
