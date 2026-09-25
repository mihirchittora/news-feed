package com.newsplatform.rbac.repository;

import com.newsplatform.rbac.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    List<Permission> findAllByOrderByCategoryAscCodeAsc();
    List<Permission> findAllByCodeIn(Collection<String> codes);
}
