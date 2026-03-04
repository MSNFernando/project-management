package com.springpm.springpm.repository;

import com.springpm.springpm.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    List<ProjectMember> findByProjectId(UUID projectId);
    
    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);
    
    void deleteByProjectId(UUID projectId);
    
    void deleteByProjectIdAndUserId(UUID projectId, UUID userId);
    
    @Query("SELECT COUNT(pm) FROM ProjectMember pm WHERE pm.projectId = :projectId")
    long countByProjectId(@Param("projectId") UUID projectId);
}
