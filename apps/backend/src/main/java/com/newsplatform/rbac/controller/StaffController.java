package com.newsplatform.rbac.controller;

import com.newsplatform.common.security.AuthenticatedUser;
import com.newsplatform.rbac.dto.StaffCreateRequest;
import com.newsplatform.rbac.dto.StaffResponse;
import com.newsplatform.rbac.dto.StaffRolesRequest;
import com.newsplatform.rbac.dto.StaffUpdateRequest;
import com.newsplatform.rbac.service.StaffService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/staff")
@Tag(name = "Staff")
@SecurityRequirement(name = "bearerAuth")
public class StaffController {
    private final StaffService staffService;

    public StaffController(StaffService staffService) { this.staffService = staffService; }

    @GetMapping
    @PreAuthorize("hasAuthority('STAFF_VIEW') or hasAuthority('SUPER_ADMIN')")
    public List<StaffResponse> list(@RequestParam(required = false) String search) { return staffService.list(search); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STAFF_VIEW') or hasAuthority('SUPER_ADMIN')")
    public StaffResponse get(@PathVariable UUID id) { return staffService.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('STAFF_CREATE') or hasAuthority('SUPER_ADMIN')")
    public StaffResponse create(@Valid @RequestBody StaffCreateRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        return staffService.create(request, actor.id());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STAFF_EDIT') or hasAuthority('SUPER_ADMIN')")
    public StaffResponse update(@PathVariable UUID id, @Valid @RequestBody StaffUpdateRequest request,
                                @AuthenticationPrincipal AuthenticatedUser actor) {
        return staffService.update(id, request, actor.id());
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('STAFF_DISABLE') or hasAuthority('SUPER_ADMIN')")
    public StaffResponse disable(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) {
        return staffService.disable(id, actor.id());
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('STAFF_DISABLE') or hasAuthority('SUPER_ADMIN')")
    public StaffResponse enable(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser actor) {
        return staffService.enable(id, actor.id());
    }

    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('STAFF_VIEW') or hasAuthority('SUPER_ADMIN')")
    public StaffResponse roles(@PathVariable UUID id) { return staffService.get(id); }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('STAFF_ROLE_ASSIGN') or hasAuthority('SUPER_ADMIN')")
    public StaffResponse updateRoles(@PathVariable UUID id, @Valid @RequestBody StaffRolesRequest request,
                                     @AuthenticationPrincipal AuthenticatedUser actor) {
        return staffService.updateRoles(id, request, actor.id());
    }
}
