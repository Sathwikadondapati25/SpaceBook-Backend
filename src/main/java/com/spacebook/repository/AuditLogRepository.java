package com.spacebook.repository;

import com.spacebook.entity.AuditLog;
import com.spacebook.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByActor(User actor);

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<AuditLog> findByTimestampAfter(LocalDateTime timestamp);

    List<AuditLog> findByActorOrderByTimestampDesc(User actor);
}
