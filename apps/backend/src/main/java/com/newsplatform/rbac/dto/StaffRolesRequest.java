package com.newsplatform.rbac.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;
import java.util.UUID;

public record StaffRolesRequest(@NotEmpty(message = "At least one role is required") Set<UUID> roleIds) {
}
