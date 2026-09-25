package com.newsplatform.rbac;

import com.newsplatform.common.error.RbacException;
import com.newsplatform.rbac.dto.RoleRequest;
import com.newsplatform.rbac.entity.Permission;
import com.newsplatform.rbac.entity.RoleEntity;
import com.newsplatform.rbac.repository.PermissionRepository;
import com.newsplatform.rbac.repository.RoleRepository;
import com.newsplatform.rbac.service.AuditService;
import com.newsplatform.rbac.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private Permission permission;

    @Test
    void createsCustomRoleFromCatalogPermissions() {
        RoleService roleService = new RoleService(roleRepository, permissionRepository, auditService);
        UUID actorId = UUID.randomUUID();
        when(roleRepository.existsByCode("EDITOR")).thenReturn(false);
        when(roleRepository.existsByNameIgnoreCase("Editors")).thenReturn(false);
        when(permission.getCode()).thenReturn("STORY_EDIT");
        when(permissionRepository.findAllByCodeIn(Set.of("STORY_EDIT"))).thenReturn(List.of(permission));
        when(roleRepository.save(any(RoleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = roleService.create(
                new RoleRequest(" Editors ", "editor", "Story editors", Set.of("STORY_EDIT"), null),
                actorId
        );

        assertThat(response.name()).isEqualTo("Editors");
        assertThat(response.code()).isEqualTo("EDITOR");
        assertThat(response.permissionCodes()).containsExactly("STORY_EDIT");
        verify(auditService).record(actorId, "ROLE_CREATED", "ROLE", response.id(),
                java.util.Map.of("code", "EDITOR", "name", "Editors"));
    }

    @Test
    void protectsSystemRolesFromMutation() {
        RoleEntity systemRole = new RoleEntity("Super Administrator", "SUPER_ADMIN", "Protected");
        ReflectionTestUtils.setField(systemRole, "systemRole", true);
        UUID roleId = UUID.randomUUID();
        when(roleRepository.findWithPermissionsById(roleId)).thenReturn(Optional.of(systemRole));
        RoleService roleService = new RoleService(roleRepository, permissionRepository, auditService);

        assertThatThrownBy(() -> roleService.update(
                roleId,
                new RoleRequest("Changed", "SUPER_ADMIN", "", Set.of(), null),
                UUID.randomUUID()))
                .isInstanceOfSatisfying(RbacException.class, exception -> {
                    assertThat(exception.getStatus().value()).isEqualTo(409);
                    assertThat(exception.getCode()).isEqualTo("SYSTEM_ROLE_PROTECTED");
                });
    }
}
