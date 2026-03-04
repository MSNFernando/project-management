package com.springpm.springpm.repository;

import com.springpm.springpm.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByWorkspaceIdAndCode(UUID workspaceId, String code);
    
    List<Permission> findByWorkspaceId(UUID workspaceId);
}
