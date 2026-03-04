package com.springpm.springpm.repository;

import com.springpm.springpm.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {
    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);
    
    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.workspace.id = ?1 AND wm.isActive = true")
    List<WorkspaceMember> findActiveByWorkspaceId(UUID workspaceId);
    
    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.workspace.id = ?1 AND wm.user.id = ?2 AND wm.isActive = true")
    Optional<WorkspaceMember> findActiveMember(UUID workspaceId, UUID userId);
    
    List<WorkspaceMember> findByWorkspaceId(UUID workspaceId);
    
    void deleteByWorkspaceId(UUID workspaceId);
}
