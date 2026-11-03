package com.springpm.springpm.service;

import com.springpm.springpm.entity.*;
import com.springpm.springpm.repository.*;
import com.springpm.springpm.util.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public TaskService(TaskRepository taskRepository,
                       TaskCommentRepository taskCommentRepository,
                       ProjectRepository projectRepository,
                       ProjectMemberRepository projectMemberRepository,
                       UserRepository userRepository,
                       WorkspaceMemberRepository workspaceMemberRepository) {
        this.taskRepository = taskRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    /**
     * Get all tasks in a project
     */
    public List<TaskDTO> listTasks(UUID workspaceId, UUID projectId) {
        validateWorkspaceAndProjectAccess(workspaceId, projectId);

        return taskRepository.findByProjectId(projectId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get task by ID
     */
    public TaskDTO getTask(UUID workspaceId, UUID projectId, UUID taskId) {
        validateWorkspaceAndProjectAccess(workspaceId, projectId);

        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        return convertToDTO(task);
    }

    /**
     * Create new task
     */
    public TaskDTO createTask(UUID workspaceId, UUID projectId, String title, String description, String type, String status, String priority, UUID assigneeId, LocalDateTime dueDate) {
        validateWorkspaceAndProjectAccess(workspaceId, projectId);

        Task task = new Task();
        task.setProjectId(projectId);
        task.setTitle(title);
        task.setDescription(description);
        task.setType(type);
        task.setStatus(status);
        task.setPriority(priority);
        task.setAssigneeId(assigneeId);
        task.setDueDate(dueDate);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task = taskRepository.save(task);

        return convertToDTO(task);
    }

    /**
     * Update task
     */
    public TaskDTO updateTask(UUID workspaceId, UUID projectId, UUID taskId, String title, String description, String type, String status, String priority, UUID assigneeId, LocalDateTime dueDate) {
        validateWorkspaceAndProjectAccess(workspaceId, projectId);

        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (title != null) task.setTitle(title);
        if (description != null) task.setDescription(description);
        if (type != null) task.setType(type);
        if (status != null) task.setStatus(status);
        if (priority != null) task.setPriority(priority);
        if (assigneeId != null) task.setAssigneeId(assigneeId);
        if (dueDate != null) task.setDueDate(dueDate);
        task.setUpdatedAt(LocalDateTime.now());
        task = taskRepository.save(task);

        return convertToDTO(task);
    }

    /**
     * Delete task
     */
    public void deleteTask(UUID workspaceId, UUID projectId, UUID taskId) {
        validateWorkspaceAndProjectAccess(workspaceId, projectId);

        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        // Delete all comments
        taskCommentRepository.deleteByTaskId(taskId);
        taskRepository.delete(task);
    }

    /**
     * Add comment to task
     */
    public TaskCommentDTO addComment(UUID workspaceId, UUID projectId, UUID taskId, String content) {
        validateWorkspaceAndProjectAccess(workspaceId, projectId);
        UUID userId = TenantContext.getCurrentUserIdRequired();

        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        TaskComment comment = new TaskComment();
        comment.setTaskId(taskId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        comment = taskCommentRepository.save(comment);

        User user = userRepository.findById(userId).orElse(null);
        return convertCommentToDTO(comment, user);
    }

    /**
     * Get task comments
     */
    public List<TaskCommentDTO> getTaskComments(UUID workspaceId, UUID projectId, UUID taskId) {
        validateWorkspaceAndProjectAccess(workspaceId, projectId);

        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        return taskCommentRepository.findByTaskId(taskId).stream()
                .map(comment -> {
                    User user = userRepository.findById(comment.getUserId()).orElse(null);
                    return convertCommentToDTO(comment, user);
                })
                .collect(Collectors.toList());
    }

    /**
     * Delete comment
     */
    public void deleteComment(UUID workspaceId, UUID projectId, UUID taskId, UUID commentId) {
        validateWorkspaceAndProjectAccess(workspaceId, projectId);
        UUID userId = TenantContext.getCurrentUserIdRequired();

        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        // Only owner or admin can delete
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Can only delete your own comments");
        }

        taskCommentRepository.delete(comment);
    }

    /**
     * Get user's assigned tasks
     */
    public List<TaskDTO> getAssignedTasks(UUID workspaceId) {
        validateWorkspaceAccess(workspaceId);
        UUID userId = TenantContext.getCurrentUserIdRequired();

        return taskRepository.findByAssigneeIdAndProjectWorkspaceId(userId, workspaceId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Helper methods
    private void validateWorkspaceAccess(UUID workspaceId) {
        UUID userId = TenantContext.getCurrentUserIdRequired();
        boolean hasAccess = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId).isPresent();
        if (!hasAccess) {
            throw new IllegalArgumentException("Access denied to workspace");
        }
    }

    private void validateWorkspaceAndProjectAccess(UUID workspaceId, UUID projectId) {
        validateWorkspaceAccess(workspaceId);
        UUID userId = TenantContext.getCurrentUserIdRequired();

        Project project = projectRepository.findByIdAndWorkspaceId(projectId, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found or access denied"));
    }

    private TaskDTO convertToDTO(Task task) {
        User assignee = task.getAssigneeId() != null ? userRepository.findById(task.getAssigneeId()).orElse(null) : null;

        return new TaskDTO(
                task.getId(),
                task.getProjectId(),
                task.getTitle(),
                task.getDescription(),
                task.getType(),
                task.getStatus(),
                task.getPriority(),
                task.getAssigneeId(),
                assignee != null ? assignee.getName() : null,
                assignee != null ? assignee.getEmail() : null,
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    private TaskCommentDTO convertCommentToDTO(TaskComment comment, User user) {
        return new TaskCommentDTO(
                comment.getId(),
                comment.getTaskId(),
                comment.getUserId(),
                user != null ? user.getName() : "Unknown",
                user != null ? user.getEmail() : "Unknown",
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    // DTOs
    public static class TaskDTO {
        public UUID id;
        public UUID projectId;
        public String title;
        public String description;
        public String type;
        public String status;
        public String priority;
        public UUID assigneeId;
        public String assigneeName;
        public String assigneeEmail;
        public LocalDateTime dueDate;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;

        public TaskDTO(UUID id, UUID projectId, String title, String description, String type, String status, String priority, UUID assigneeId, String assigneeName, String assigneeEmail, LocalDateTime dueDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.projectId = projectId;
            this.title = title;
            this.description = description;
            this.type = type;
            this.status = status;
            this.priority = priority;
            this.assigneeId = assigneeId;
            this.assigneeName = assigneeName;
            this.assigneeEmail = assigneeEmail;
            this.dueDate = dueDate;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        // Getters
        public UUID getId() { return id; }
        public String getTitle() { return title; }
        public String getStatus() { return status; }
    }

    public static class TaskCommentDTO {
        public UUID id;
        public UUID taskId;
        public UUID userId;
        public String userName;
        public String userEmail;
        public String content;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;

        public TaskCommentDTO(UUID id, UUID taskId, UUID userId, String userName, String userEmail, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.taskId = taskId;
            this.userId = userId;
            this.userName = userName;
            this.userEmail = userEmail;
            this.content = content;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        // Getters
        public UUID getId() { return id; }
        public String getContent() { return content; }
    }
}
