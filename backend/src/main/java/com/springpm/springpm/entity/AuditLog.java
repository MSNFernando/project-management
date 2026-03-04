package com.springpm.springpm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_logs_workspace_id", columnList = "workspace_id"),
    @Index(name = "idx_audit_logs_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_logs_entity_type", columnList = "entity_type"),
    @Index(name = "idx_audit_logs_created_at", columnList = "created_at DESC"),
    @Index(name = "idx_audit_logs_composite", columnList = "workspace_id, created_at DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    private String action; // CREATE, UPDATE, DELETE, READ

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // PROJECT, TASK, USER, WORKSPACE_MEMBER, etc.

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(columnDefinition = "JSONB")
    private String oldValues;

    @Column(columnDefinition = "JSONB")
    private String newValues;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
