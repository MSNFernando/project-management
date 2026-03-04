package com.springpm.springpm.repository;

import com.springpm.springpm.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    Optional<Workspace> findBySlug(String slug);
    
    @Query("SELECT w FROM Workspace w WHERE w.owner.id = ?1 AND w.deletedAt IS NULL")
    List<Workspace> findByOwnerId(UUID ownerId);
    
    @Query("SELECT DISTINCT w FROM Workspace w " +
           "JOIN w.members m WHERE m.user.id = ?1 AND w.deletedAt IS NULL")
    List<Workspace> findByMemberId(UUID userId);
    
    @Query("SELECT w FROM Workspace w WHERE w.id = :workspaceId AND w.owner.id = :ownerId")
    Optional<Workspace> findByIdAndOwnerId(@Param("workspaceId") UUID workspaceId, @Param("ownerId") UUID ownerId);
    
    @Query("SELECT w FROM Workspace w WHERE w.id = :workspaceId")
    Optional<Workspace> findByIdForAccess(@Param("workspaceId") UUID workspaceId);
}
