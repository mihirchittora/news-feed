package com.newsplatform.rbac.controller;

import com.newsplatform.common.security.AuthenticatedUser;
import com.newsplatform.rbac.dto.PermissionCodesRequest;
import com.newsplatform.rbac.dto.PermissionGroupResponse;
import com.newsplatform.rbac.dto.RoleRequest;
import com.newsplatform.rbac.dto.RoleResponse;
import com.newsplatform.rbac.service.RoleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Roles and permissions")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) { this.roleService = roleService; }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ROLE_VIEW') or hasAuthority('SUPER_ADMIN')")
    public List<RoleResponse> list() { return roleService.listRoles(); }

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('ROLE_VIEW') or hasAuthority('SUPER_ADMIN')")
    public RoleResponse get(@PathVariable UUID id) { return roleService.getRole(id); }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_CREATE') or hasAuthority('SUPER_ADMIN')")
    public RoleResponse create(@Valid @RequestBody RoleRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        return roleService.create(request, actor.id());
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('ROLE_EDIT') or hasAuthority('SUPER_ADMIN')")
    public RoleResponse update(@PathVariable UUID id, @Valid @RequestBody RoleRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        return roleService.update(id, request, actor.id());
    }

    @DeleteMapping("/roles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_DELETE') or hasAuthority('SUPER_ADMIN')")
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) {
        roleService.delete(id, actor.id());
    }

    @GetMapping("/roles/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_VIEW') or hasAuthority('SUPER_ADMIN')")
    public RoleResponse permissions(@PathVariable UUID id) { return roleService.getRole(id); }

    @PutMapping("/roles/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_PERMISSION_ASSIGN') or hasAuthority('SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePermissions(@PathVariable UUID id, @Valid @RequestBody PermissionCodesRequest request,
                                  @AuthenticationPrincipal AuthenticatedUser actor) {
        roleService.updatePermissions(id, request, actor.id());
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLE_VIEW') or hasAuthority('SUPER_ADMIN')")
    public List<PermissionGroupResponse> permissions() { return roleService.listPermissions(); }
}
