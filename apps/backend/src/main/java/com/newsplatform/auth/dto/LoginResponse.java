package com.newsplatform.auth.dto;

import com.newsplatform.user.dto.UserResponse;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
}
