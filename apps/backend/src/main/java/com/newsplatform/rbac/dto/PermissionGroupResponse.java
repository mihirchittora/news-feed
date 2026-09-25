package com.newsplatform.rbac.dto;

import java.util.List;

public record PermissionGroupResponse(String category, List<PermissionResponse> permissions) {
}
