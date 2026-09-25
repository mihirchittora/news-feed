package com.newsplatform.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StaffSetupRequest(
        @NotBlank(message = "Setup token is required") String token,
        @NotBlank(message = "Password is required") @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters") String password
) {
}
