package com.newsplatform.rbac.dto;

import com.newsplatform.user.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StaffResponse(UUID id, String name, String email, String status, List<RoleSummary> roles,
                            Instant createdAt, Instant updatedAt, String setupLink) {
    public static StaffResponse from(User user) {
        return from(user, null);
    }

    public static StaffResponse from(User user, String setupLink) {
        return new StaffResponse(user.getId(), user.getName(), user.getEmail(), user.getStatus().name(),
                user.getRoles().stream().map(RoleSummary::from).sorted(java.util.Comparator.comparing(RoleSummary::name)).toList(),
                user.getCreatedAt(), user.getUpdatedAt(), setupLink);
    }
}
