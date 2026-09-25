package com.newsplatform.user.repository;

import com.newsplatform.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findWithRolesById(UUID id);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<User> findWithRolesByEmail(String email);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    java.util.List<User> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    java.util.List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByNameAsc(String name, String email);

    long countByStatusAndRoles_Code(com.newsplatform.user.entity.UserStatus status, String code);
}
