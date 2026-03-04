package com.springpm.springpm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workspace_members", indexes = {
    @Index(name = "idx_workspace_members_workspace_id", columnList = "workspace_id"),
    @Index(name = "idx_workspace_members_user_id", columnList = "user_id"),
    @Index(name = "idx_workspace_members_role_id", columnList = "role_id"),
    @Index(name = "idx_workspace_members_composite", columnList = "workspace_id, user_id")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"workspace_id", "user_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false, insertable = false, updatable = false)
    private Workspace workspace;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String role; // ADMIN, MEMBER, VIEWER

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "joined_at")
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
