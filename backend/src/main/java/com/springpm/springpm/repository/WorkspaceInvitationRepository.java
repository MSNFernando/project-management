package com.springpm.springpm.repository;

import com.springpm.springpm.entity.WorkspaceInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitation, UUID> {
    Optional<WorkspaceInvitation> findByToken(String token);
    
    Optional<WorkspaceInvitation> findByWorkspaceIdAndEmailAndIsAcceptedFalse(UUID workspaceId, String email);
}
