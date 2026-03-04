package com.springpm.springpm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "projects", indexes = {
    @Index(name = "idx_projects_workspace_id", columnList = "workspace_id"),
    @Index(name = "idx_projects_team_lead_id", columnList = "team_lead_id"),
    @Index(name = "idx_projects_status", columnList = "status"),
    @Index(name = "idx_projects_deleted_at", columnList = "deleted_at"),
    @Index(name = "idx_projects_composite", columnList = "workspace_id, is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false, insertable = false, updatable = false)
    private Workspace workspace;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PLANNING"; // PLANNING, ACTIVE, COMPLETED, ON_HOLD, CANCELLED

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH

    @Column(nullable = false)
    @Builder.Default
    private Integer progress = 0;

    @Column(name = "team_lead_id")
    private UUID teamLeadId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_lead_id", insertable = false, updatable = false)
    private User teamLead;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "projectId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectMember> members = new ArrayList<>();
}
