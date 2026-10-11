package com.spacebook.service;

import com.spacebook.entity.AuditLog;
import com.spacebook.entity.User;
import com.spacebook.exception.ResourceNotFoundException;
import com.spacebook.repository.AuditLogRepository;
import com.spacebook.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Audit logging service for tracking important actions
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditLogRepository auditLogRepository, UserRepository userRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AuditLog createAuditLog(Long actorId, String action, String entityType, Long entityId, String details) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String jsonDetails = details;
        if (details != null && !details.trim().isEmpty()) {
            String trimmed = details.trim();
            if (!(trimmed.startsWith("{") && trimmed.endsWith("}")) && !(trimmed.startsWith("[") && trimmed.endsWith("]"))) {
                try {
                    jsonDetails = objectMapper.writeValueAsString(Map.of("message", details));
                } catch (Exception e) {
                    jsonDetails = "{\"message\":\"" + details.replace("\"", "\\\"") + "\"}";
                }
            }
        } else {
            jsonDetails = "{}";
        }

        AuditLog auditLog = AuditLog.builder()
                .actor(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(jsonDetails)
                .build();

        return auditLogRepository.save(auditLog);
    }

    @Transactional
    public AuditLog createAuditLogWithDetails(Long actorId, String action, String entityType, Long entityId, Map<String, Object> detailsMap) {
        String detailsJson = null;
        if (detailsMap != null && !detailsMap.isEmpty()) {
            try {
                detailsJson = objectMapper.writeValueAsString(detailsMap);
            } catch (JsonProcessingException e) {
                detailsJson = "{}";
            }
        }

        return createAuditLog(actorId, action, entityType, entityId, detailsJson);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getUserAuditLogs(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return auditLogRepository.findByActorOrderByTimestampDesc(user);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getEntityAuditLogs(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getRecentAuditLogs(LocalDateTime since) {
        return auditLogRepository.findByTimestampAfter(since);
    }
}
