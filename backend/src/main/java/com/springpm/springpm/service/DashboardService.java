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
@Transactional(readOnly = true)
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TaskCommentRepository taskCommentRepository;

    public DashboardService(ProjectRepository projectRepository,
                           TaskRepository taskRepository,
                           WorkspaceMemberRepository workspaceMemberRepository,
                           ProjectMemberRepository projectMemberRepository,
                           UserRepository userRepository,
                           TaskCommentRepository taskCommentRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.taskCommentRepository = taskCommentRepository;
    }

    /**
     * Get dashboard statistics
     */
    public DashboardStatsDTO getDashboardStats(UUID workspaceId) {
        validateWorkspaceAccess(workspaceId);

        List<Project> projects = projectRepository.findByWorkspaceId(workspaceId);
        List<Task> allTasks = projects.stream()
                .flatMap(p -> taskRepository.findByProjectId(p.getId()).stream())
                .collect(Collectors.toList());

        // Count projects by status
        long activeProjects = projects.stream().filter(p -> "ACTIVE".equals(p.getStatus())).count();
        long completedProjects = projects.stream().filter(p -> "COMPLETED".equals(p.getStatus())).count();
        long planningProjects = projects.stream().filter(p -> "PLANNING".equals(p.getStatus())).count();

        // Count tasks by status
        long completedTasks = allTasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        long inProgressTasks = allTasks.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        long todoTasks = allTasks.stream().filter(t -> "TODO".equals(t.getStatus())).count();

        // Team size
        List<WorkspaceMember> members = workspaceMemberRepository.findByWorkspaceId(workspaceId);

        // Calculate averages
        double avgProjectProgress = projects.isEmpty() ? 0 : projects.stream()
                .mapToInt(Project::getProgress)
                .average()
                .orElse(0.0);

        return new DashboardStatsDTO(
                projects.size(),
                (int) activeProjects,
                (int) completedProjects,
                (int) planningProjects,
                allTasks.size(),
                (int) completedTasks,
                (int) inProgressTasks,
                (int) todoTasks,
                members.size(),
                (int) avgProjectProgress
        );
    }

    /**
     * Get recent activity
     */
    public List<ActivityDTO> getRecentActivity(UUID workspaceId) {
        validateWorkspaceAccess(workspaceId);

        List<Project> projects = projectRepository.findByWorkspaceId(workspaceId);
        List<Task> allTasks = projects.stream()
                .flatMap(p -> taskRepository.findByProjectId(p.getId()).stream())
                .collect(Collectors.toList());

        List<ActivityDTO> activities = new ArrayList<>();

        // Add project creation activities
        projects.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .forEach(p -> {
                    User owner = userRepository.findById(p.getTeamLeadId()).orElse(null);
                    activities.add(new ActivityDTO(
                            "PROJECT_CREATED",
                            p.getName(),
                            owner != null ? owner.getName() : "Unknown",
                            p.getCreatedAt()
                    ));
                });

        // Add task activities (updated tasks)
        allTasks.stream()
                .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                .limit(5)
                .forEach(t -> {
                    activities.add(new ActivityDTO(
                            "TASK_UPDATED",
                            t.getTitle(),
                            t.getStatus(),
                            t.getUpdatedAt()
                    ));
                });

        // Sort by timestamp descending and take last 10
        return activities.stream()
                .sorted((a, b) -> b.timestamp.compareTo(a.timestamp))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Get user's task summary
     */
    public List<UserTaskSummaryDTO> getUserTaskSummary(UUID workspaceId) {
        validateWorkspaceAccess(workspaceId);
        UUID userId = TenantContext.getCurrentUserIdRequired();

        List<Task> userTasks = taskRepository.findByAssigneeIdAndProjectWorkspaceId(userId, workspaceId);

        return userTasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus))
                .entrySet().stream()
                .map(entry -> new UserTaskSummaryDTO(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream()
                                .map(t -> new UserTaskSummaryDTO.TaskSummary(t.getId(), t.getTitle(), t.getPriority()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get project overview (ongoing projects)
     */
    public List<ProjectOverviewDTO> getProjectOverview(UUID workspaceId) {
        validateWorkspaceAccess(workspaceId);

        return projectRepository.findByWorkspaceId(workspaceId).stream()
                .filter(p -> !("COMPLETED".equals(p.getStatus()) || "CANCELLED".equals(p.getStatus())))
                .map(p -> {
                    List<Task> tasks = taskRepository.findByProjectId(p.getId());
                    long completedTasks = tasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
                    User lead = userRepository.findById(p.getTeamLeadId()).orElse(null);

                    return new ProjectOverviewDTO(
                            p.getId(),
                            p.getName(),
                            p.getStatus(),
                            p.getProgress(),
                            (int) completedTasks,
                            tasks.size(),
                            lead != null ? lead.getName() : "Unknown",
                            p.getEndDate()
                    );
                })
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

    // DTOs
    public static class DashboardStatsDTO {
        public int totalProjects;
        public int activeProjects;
        public int completedProjects;
        public int planningProjects;
        public int totalTasks;
        public int completedTasks;
        public int inProgressTasks;
        public int todoTasks;
        public int teamMembers;
        public int averageProgress;

        public DashboardStatsDTO(int totalProjects, int activeProjects, int completedProjects, int planningProjects, int totalTasks, int completedTasks, int inProgressTasks, int todoTasks, int teamMembers, int averageProgress) {
            this.totalProjects = totalProjects;
            this.activeProjects = activeProjects;
            this.completedProjects = completedProjects;
            this.planningProjects = planningProjects;
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
            this.inProgressTasks = inProgressTasks;
            this.todoTasks = todoTasks;
            this.teamMembers = teamMembers;
            this.averageProgress = averageProgress;
        }

        // Getters
        public int getTotalProjects() { return totalProjects; }
        public int getActiveProjects() { return activeProjects; }
    }

    public static class ActivityDTO {
        public String type;
        public String title;
        public String description;
        public LocalDateTime timestamp;

        public ActivityDTO(String type, String title, String description, LocalDateTime timestamp) {
            this.type = type;
            this.title = title;
            this.description = description;
            this.timestamp = timestamp;
        }

        // Getters
        public String getType() { return type; }
        public String getTitle() { return title; }
    }

    public static class UserTaskSummaryDTO {
        public String status;
        public int count;
        public List<TaskSummary> tasks;

        public UserTaskSummaryDTO(String status, int count, List<TaskSummary> tasks) {
            this.status = status;
            this.count = count;
            this.tasks = tasks;
        }

        public static class TaskSummary {
            public UUID id;
            public String title;
            public String priority;

            public TaskSummary(UUID id, String title, String priority) {
                this.id = id;
                this.title = title;
                this.priority = priority;
            }

            public UUID getId() { return id; }
            public String getTitle() { return title; }
        }

        // Getters
        public String getStatus() { return status; }
        public int getCount() { return count; }
    }

    public static class ProjectOverviewDTO {
        public UUID id;
        public String name;
        public String status;
        public int progress;
        public int completedTasks;
        public int totalTasks;
        public String teamLead;
        public LocalDateTime endDate;

        public ProjectOverviewDTO(UUID id, String name, String status, int progress, int completedTasks, int totalTasks, String teamLead, LocalDateTime endDate) {
            this.id = id;
            this.name = name;
            this.status = status;
            this.progress = progress;
            this.completedTasks = completedTasks;
            this.totalTasks = totalTasks;
            this.teamLead = teamLead;
            this.endDate = endDate;
        }

        // Getters
        public UUID getId() { return id; }
        public String getName() { return name; }
        public int getProgress() { return progress; }
    }
}
