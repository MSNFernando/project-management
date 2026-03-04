package com.springpm.springpm.repository;

import com.springpm.springpm.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, UUID> {
    List<UserGroup> findByWorkspaceId(UUID workspaceId);
}
