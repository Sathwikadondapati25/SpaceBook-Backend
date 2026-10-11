package com.spacebook.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Audit log for tracking important actions
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "JSON")
    private String details;

    public AuditLog() {
    }

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private User actor;
        private String action;
        private String entityType;
        private Long entityId;
        private String details;

        public Builder actor(User actor) { this.actor = actor; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder entityType(String entityType) { this.entityType = entityType; return this; }
        public Builder entityId(Long entityId) { this.entityId = entityId; return this; }
        public Builder details(String details) { this.details = details; return this; }

        public AuditLog build() {
            AuditLog log = new AuditLog();
            log.setActor(this.actor);
            log.setAction(this.action);
            log.setEntityType(this.entityType);
            log.setEntityId(this.entityId);
            log.setDetails(this.details);
            return log;
        }
    }
}
