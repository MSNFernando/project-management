package com.springpm.springpm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permissions_workspace_id", columnList = "workspace_id"),
    @Index(name = "idx_permissions_resource_type", columnList = "resource_type")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"workspace_id", "code"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
