package com.newsplatform.rbac.repository;

import com.newsplatform.rbac.entity.RoleEntity;
import com.newsplatform.rbac.entity.RoleStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByNameIgnoreCase(String name);

    @EntityGraph(attributePaths = "permissions")
    List<RoleEntity> findAllByOrderBySystemRoleDescNameAsc();

    @EntityGraph(attributePaths = "permissions")
    Optional<RoleEntity> findWithPermissionsById(UUID id);

    List<RoleEntity> findAllByIdInAndStatus(Collection<UUID> ids, RoleStatus status);

    long countByCodeAndStatus(String code, RoleStatus status);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<RoleEntity> findByCodeAndStatus(String code, RoleStatus status);
}
