package com.newsplatform.rbac.repository;

import com.newsplatform.rbac.entity.StaffSetupToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StaffSetupTokenRepository extends JpaRepository<StaffSetupToken, UUID> {
    Optional<StaffSetupToken> findByTokenHash(String tokenHash);
}
