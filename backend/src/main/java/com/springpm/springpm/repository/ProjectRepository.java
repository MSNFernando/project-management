package com.springpm.springpm.repository;

import com.springpm.springpm.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    @Query("SELECT p FROM Project p WHERE p.workspace.id = ?1 AND p.isActive = true AND p.deletedAt IS NULL")
    List<Project> findActiveByWorkspaceId(UUID workspaceId);
    
    @Query("SELECT p FROM Project p WHERE p.workspace.id = ?1 AND p.isActive = true AND p.deletedAt IS NULL")
    Page<Project> findActiveByWorkspaceId(UUID workspaceId, Pageable pageable);
    
    @Query("SELECT p FROM Project p WHERE p.workspace.id = ?1 AND p.status = ?2 AND p.deletedAt IS NULL")
    List<Project> findByWorkspaceIdAndStatus(UUID workspaceId, String status);
    
    @Query("SELECT p FROM Project p WHERE p.teamLead.id = ?1 AND p.workspace.id = ?2 AND p.deletedAt IS NULL")
    List<Project> findByTeamLeadAndWorkspace(UUID teamLeadId, UUID workspaceId);
    
    // New queries for basic operations
    List<Project> findByWorkspaceId(UUID workspaceId);
    
    Optional<Project> findByIdAndWorkspaceId(UUID projectId, UUID workspaceId);
    
    Optional<Project> findByIdAndTeamLeadId(UUID projectId, UUID teamLeadId);
    
    @Query("SELECT p FROM Project p WHERE p.workspaceId = :workspaceId")
    List<Project> findByWorkspaceIdForMember(@Param("workspaceId") UUID workspaceId);
}
