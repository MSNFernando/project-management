package com.springpm.springpm.repository;

import com.springpm.springpm.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {
    @Query("SELECT tc FROM TaskComment tc WHERE tc.taskId = ?1 AND tc.deletedAt IS NULL ORDER BY tc.createdAt DESC")
    List<TaskComment> findByTaskId(UUID taskId);

    void deleteByTaskId(UUID taskId);
}
