package com.newsplatform.rbac.repository;

import com.newsplatform.rbac.entity.StaffSetupToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface StaffSetupTokenRepository extends JpaRepository<StaffSetupToken, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StaffSetupToken> findByTokenHash(String tokenHash);
}
