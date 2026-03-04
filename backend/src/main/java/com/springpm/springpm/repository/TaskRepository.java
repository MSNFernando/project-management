package com.springpm.springpm.repository;

import com.springpm.springpm.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    
    // Original queries
    @Query("SELECT t FROM Task t WHERE t.project.id = ?1 AND t.isActive = true AND t.deletedAt IS NULL")
    List<Task> findActiveByProjectId(UUID projectId);
    
    @Query("SELECT t FROM Task t WHERE t.assignee.id = ?1 AND t.project.workspace.id = ?2 AND t.status != 'DONE' AND t.deletedAt IS NULL")
    List<Task> findAssignedToUserInWorkspace(UUID userId, UUID workspaceId);
    
    @Query("SELECT t FROM Task t WHERE t.status = ?1 AND t.project.id = ?2 AND t.deletedAt IS NULL")
    List<Task> findByStatusAndProject(String status, UUID projectId);
    
    @Query("SELECT t FROM Task t WHERE t.project.id = ?1 AND t.status = ?2 AND t.deletedAt IS NULL")
    int countByProjectAndStatus(UUID projectId, String status);
    
    // New queries for basic operations
    List<Task> findByProjectId(UUID projectId);
    
    Optional<Task> findByIdAndProjectId(UUID taskId, UUID projectId);
    
    void deleteByProjectId(UUID projectId);
    
    long countByProjectId(UUID projectId);
    
    @Query("SELECT t FROM Task t WHERE t.assigneeId = :assigneeId AND t.projectId IN (SELECT p.id FROM Project p WHERE p.workspaceId = :workspaceId)")
    List<Task> findByAssigneeIdAndProjectWorkspaceId(@Param("assigneeId") UUID assigneeId, @Param("workspaceId") UUID workspaceId);
}
