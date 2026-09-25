package com.newsplatform.common.security;

import com.newsplatform.user.entity.Role;

import java.util.UUID;

public record AuthenticatedUser(UUID id, Role role) {
}
