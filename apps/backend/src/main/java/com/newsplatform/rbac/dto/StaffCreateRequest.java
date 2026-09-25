package com.newsplatform.rbac.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record StaffCreateRequest(
        @NotBlank(message = "Name is required") @Size(max = 120, message = "Name must be 120 characters or fewer") String name,
        @NotBlank(message = "Email is required") @Email @Size(max = 320, message = "Email must be 320 characters or fewer") String email,
        @NotEmpty(message = "At least one role is required") Set<UUID> roleIds
) {
}
