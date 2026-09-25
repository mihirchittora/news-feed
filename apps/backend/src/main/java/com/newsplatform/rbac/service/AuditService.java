package com.newsplatform.rbac.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsplatform.rbac.entity.AdminAuditLog;
import com.newsplatform.rbac.repository.AdminAuditLogRepository;
import com.newsplatform.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {
    private final AdminAuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AdminAuditLogRepository auditLogRepository, UserRepository userRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public void record(UUID actorUserId, String action, String targetType, UUID targetId, Map<String, ?> metadata) {
        String serialized = null;
        if (metadata != null && !metadata.isEmpty()) {
            try {
                serialized = objectMapper.writeValueAsString(metadata);
            } catch (JsonProcessingException ignored) {
                serialized = "{}";
            }
        }
        auditLogRepository.save(new AdminAuditLog(
                actorUserId == null ? null : userRepository.getReferenceById(actorUserId),
                action, targetType, targetId, serialized
        ));
    }
}
