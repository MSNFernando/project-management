package com.springpm.springpm.service;

import com.springpm.springpm.entity.*;
import com.springpm.springpm.repository.*;
import com.springpm.springpm.util.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository,
                         ProjectMemberRepository projectMemberRepository,
                         TaskRepository taskRepository,
                         WorkspaceMemberRepository workspaceMemberRepository,
                         UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get all projects in workspace
     */
    public List<ProjectDTO> listProjects(UUID workspaceId) {
        validateWorkspaceAccess(workspaceId);

        return projectRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get project by ID
     */
    public ProjectDTO getProject(UUID workspaceId, UUID projectId) {
        validateWorkspaceAccess(workspaceId);

        Project project = projectRepository.findByIdAndWorkspaceId(projectId, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        return convertToDTO(project);
    }

    /**
     * Create new project
     */
    public ProjectDTO createProject(UUID workspaceId, String name, String description, String status, String priority, LocalDateTime startDate, LocalDateTime endDate) {
        validateWorkspaceAccess(workspaceId);
        UUID userId = TenantContext.getCurrentUserIdRequired();

        Project project = new Project();
        project.setWorkspaceId(workspaceId);
        project.setName(name);
        project.setDescription(description);
        project.setStatus(status);
        project.setPriority(priority);
        project.setProgress(0);
        project.setTeamLeadId(userId);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        project = projectRepository.save(project);

        // Add creator as member
        ProjectMember member = new ProjectMember();
        member.setProjectId(project.getId());
        member.setUserId(userId);
        member.setJoinedAt(LocalDateTime.now());
        projectMemberRepository.save(member);

        return convertToDTO(project);
    }

    /**
     * Update project
     */
    public ProjectDTO updateProject(UUID workspaceId, UUID projectId, String name, String description, String status, String priority, Integer progress, LocalDateTime startDate, LocalDateTime endDate) {
        validateWorkspaceAccess(workspaceId);
        validateProjectAccess(projectId);

        Project project = projectRepository.findByIdAndWorkspaceId(projectId, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (name != null) project.setName(name);
        if (description != null) project.setDescription(description);
        if (status != null) project.setStatus(status);
        if (priority != null) project.setPriority(priority);
        if (progress != null) project.setProgress(progress);
        if (startDate != null) project.setStartDate(startDate);
        if (endDate != null) project.setEndDate(endDate);
        project.setUpdatedAt(LocalDateTime.now());
        project = projectRepository.save(project);

        return convertToDTO(project);
    }

    /**
     * Delete project
     */
    public void deleteProject(UUID workspaceId, UUID projectId) {
        validateWorkspaceAccess(workspaceId);
        validateProjectAccess(projectId);

        Project project = projectRepository.findByIdAndWorkspaceId(projectId, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // Delete all tasks and members
        taskRepository.deleteByProjectId(projectId);
        projectMemberRepository.deleteByProjectId(projectId);
        projectRepository.delete(project);
    }

    /**
     * Get project members
     */
    public List<ProjectMemberDTO> getProjectMembers(UUID workspaceId, UUID projectId) {
        validateWorkspaceAccess(workspaceId);
        validateProjectAccess(projectId);

        return projectMemberRepository.findByProjectId(projectId).stream()
                .map(member -> {
                    User user = userRepository.findById(member.getUserId()).orElse(null);
                    return convertMemberToDTO(member, user);
                })
                .collect(Collectors.toList());
    }

    /**
     * Add member to project
     */
    public ProjectMemberDTO addMemberToProject(UUID workspaceId, UUID projectId, UUID userId) {
        validateWorkspaceAccess(workspaceId);
        validateProjectAccess(projectId);

        // Check if already member
        if (projectMemberRepository.findByProjectIdAndUserId(projectId, userId).isPresent()) {
            throw new IllegalArgumentException("User already member of project");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setJoinedAt(LocalDateTime.now());
        member = projectMemberRepository.save(member);

        return convertMemberToDTO(member, user);
    }

    /**
     * Remove member from project
     */
    public void removeMemberFromProject(UUID workspaceId, UUID projectId, UUID userId) {
        validateWorkspaceAccess(workspaceId);
        validateProjectAccess(projectId);

        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not member of project"));

        projectMemberRepository.delete(member);
    }

    /**
     * Get project analytics
     */
    public ProjectAnalyticsDTO getProjectAnalytics(UUID workspaceId, UUID projectId) {
        validateWorkspaceAccess(workspaceId);
        validateProjectAccess(projectId);

        Project project = projectRepository.findByIdAndWorkspaceId(projectId, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        List<Task> tasks = taskRepository.findByProjectId(projectId);

        // Calculate stats
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        long inProgressTasks = tasks.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        long todoTasks = tasks.stream().filter(t -> "TODO".equals(t.getStatus())).count();

        int progress = totalTasks > 0 ? (int) ((completedTasks * 100) / totalTasks) : 0;

        return new ProjectAnalyticsDTO(
                projectId,
                project.getName(),
                progress,
                totalTasks,
                completedTasks,
                inProgressTasks,
                todoTasks
        );
    }

    // Helper methods
    private void validateWorkspaceAccess(UUID workspaceId) {
        UUID userId = TenantContext.getCurrentUserIdRequired();
        boolean hasAccess = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId).isPresent();
        if (!hasAccess) {
            throw new IllegalArgumentException("Access denied to workspace");
        }
    }

    private void validateProjectAccess(UUID projectId) {
        UUID userId = TenantContext.getCurrentUserIdRequired();
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Access denied to project"));
    }

    private ProjectDTO convertToDTO(Project project) {
        List<ProjectMember> members = projectMemberRepository.findByProjectId(project.getId());
        long taskCount = taskRepository.countByProjectId(project.getId());

        ProjectDTO dto = new ProjectDTO(
                project.getId(),
                project.getWorkspaceId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getPriority(),
                project.getProgress(),
                project.getTeamLeadId(),
                project.getStartDate(),
                project.getEndDate(),
                members.size(),
                (int) taskCount,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );

        // The web app reads project.members[].user.{id,name,email}
        Map<UUID, User> users = userRepository.findAllById(
                members.stream().map(ProjectMember::getUserId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(User::getId, u -> u));
        dto.members = members.stream()
                .map(m -> {
                    User u = users.get(m.getUserId());
                    return new MemberSummary(m.getId(), m.getUserId(),
                            new UserSummary(m.getUserId(), u != null ? u.getName() : null, u != null ? u.getEmail() : null),
                            m.getJoinedAt());
                })
                .collect(Collectors.toList());
        return dto;
    }

    private ProjectMemberDTO convertMemberToDTO(ProjectMember member, User user) {
        return new ProjectMemberDTO(
                member.getId(),
                member.getProjectId(),
                member.getUserId(),
                user != null ? user.getName() : "Unknown",
                user != null ? user.getEmail() : "Unknown",
                member.getJoinedAt()
        );
    }

    // DTOs
    public static class ProjectDTO {
        public UUID id;
        public UUID workspaceId;
        public String name;
        public String description;
        public String status;
        public String priority;
        public int progress;
        public UUID teamLeadId;
        public LocalDateTime startDate;
        public LocalDateTime endDate;
        public int memberCount;
        public List<MemberSummary> members = new ArrayList<>();
        public int taskCount;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;

        public ProjectDTO(UUID id, UUID workspaceId, String name, String description, String status, String priority, int progress, UUID teamLeadId, LocalDateTime startDate, LocalDateTime endDate, int memberCount, int taskCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.workspaceId = workspaceId;
            this.name = name;
            this.description = description;
            this.status = status;
            this.priority = priority;
            this.progress = progress;
            this.teamLeadId = teamLeadId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.memberCount = memberCount;
            this.taskCount = taskCount;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        // Getters
        public UUID getId() { return id; }
        public String getName() { return name; }
    }

    public static class ProjectMemberDTO {
        public UUID id;
        public UUID projectId;
        public UUID userId;
        public String userName;
        public String email;
        public LocalDateTime joinedAt;

        public ProjectMemberDTO(UUID id, UUID projectId, UUID userId, String userName, String email, LocalDateTime joinedAt) {
            this.id = id;
            this.projectId = projectId;
            this.userId = userId;
            this.userName = userName;
            this.email = email;
            this.joinedAt = joinedAt;
        }

        public UUID getUserId() { return userId; }
        public String getUserName() { return userName; }
    }

    public record UserSummary(UUID id, String name, String email) {}

    public record MemberSummary(UUID id, UUID userId, UserSummary user, LocalDateTime joinedAt) {}

    public static class ProjectAnalyticsDTO {
        public UUID projectId;
        public String projectName;
        public int progress;
        public long totalTasks;
        public long completedTasks;
        public long inProgressTasks;
        public long todoTasks;

        public ProjectAnalyticsDTO(UUID projectId, String projectName, int progress, long totalTasks, long completedTasks, long inProgressTasks, long todoTasks) {
            this.projectId = projectId;
            this.projectName = projectName;
            this.progress = progress;
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
            this.inProgressTasks = inProgressTasks;
            this.todoTasks = todoTasks;
        }
    }
}
