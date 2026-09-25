package com.newsplatform.user.dto;

import com.newsplatform.user.entity.User;
import com.newsplatform.rbac.dto.RoleSummary;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String role,
        List<RoleSummary> roles,
        Set<String> permissions,
        String status
    ) {
    public static UserResponse from(User user) {
        List<RoleSummary> roles = user.getRoles().stream()
                .map(RoleSummary::from)
                .sorted(java.util.Comparator.comparing(RoleSummary::name))
                .toList();
        Set<String> permissions = user.getRoles().stream()
                .filter(role -> role.getStatus() == com.newsplatform.rbac.entity.RoleStatus.ACTIVE)
                .flatMap(role -> role.getPermissions().stream())
                .map(com.newsplatform.rbac.entity.Permission::getCode)
                .collect(java.util.stream.Collectors.toCollection(java.util.TreeSet::new));
        if (roles.stream().anyMatch(role -> role.code().equals("SUPER_ADMIN"))) {
            permissions = Set.copyOf(permissions); // SUPER_ADMIN permissions are materialized by the seeded role.
        }
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name(), roles, permissions, user.getStatus().name());
    }
}
