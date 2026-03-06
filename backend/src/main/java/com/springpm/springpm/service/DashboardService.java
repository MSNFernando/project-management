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
     * Returns fields the frontend StatsGrid expects:
     *   totalProjects, completedProjects, myTasks, overdueIssues
     * Plus extras used elsewhere:
     *   activeProjects, planningProjects, totalTasks, completedTasks,
     *   inProgressTasks, todoTasks, teamMembers, averageProgress
     */
    public DashboardStatsDTO getDashboardStats(UUID workspaceId) {
        validateWorkspaceAccess(workspaceId);
        UUID userId = TenantContext.getCurrentUserIdRequired();

        List<Project> projects = projectRepository.findByWorkspaceId(workspaceId);
        List<Task> allTasks = projects.stream()
                .flatMap(p -> taskRepository.findByProjectId(p.getId()).stream())
                .collect(Collectors.toList());

        // Count projects by status
        long activeProjects   = projects.stream().filter(p -> "ACTIVE".equals(p.getStatus())).count();
        long completedProjects = projects.stream().filter(p -> "COMPLETED".equals(p.getStatus())).count();
        long planningProjects  = projects.stream().filter(p -> "PLANNING".equals(p.getStatus())).count();

        // Count tasks by status
        long completedTasks  = allTasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        long inProgressTasks = allTasks.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        long todoTasks       = allTasks.stream().filter(t -> "TODO".equals(t.getStatus())).count();

        // My tasks — tasks assigned to the current user
        long myTasks = allTasks.stream()
                .filter(t -> userId.equals(t.getAssigneeId()))
                .count();

        // Overdue tasks — past due date and not completed/done
        LocalDateTime now = LocalDateTime.now();
        long overdueIssues = allTasks.stream()
                .filter(t -> t.getDueDate() != null
                        && t.getDueDate().isBefore(now)
                        && !"COMPLETED".equals(t.getStatus())
                        && !"DONE".equals(t.getStatus()))
                .count();

        // Team size
        List<WorkspaceMember> members = workspaceMemberRepository.findByWorkspaceId(workspaceId);

        // Average project progress
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
                (int) avgProjectProgress,
                (int) myTasks,
                (int) overdueIssues
        );
    }

    /**
     * Get recent activity.
     * Returns task-like objects shaped for RecentActivity.jsx:
     *   id, title, status, type, updatedAt, assignee { fullName }
     */
    public List<ActivityDTO> getRecentActivity(UUID workspaceId) {
        validateWorkspaceAccess(workspaceId);

        List<Project> projects = projectRepository.findByWorkspaceId(workspaceId);
        List<Task> allTasks = projects.stream()
                .flatMap(p -> taskRepository.findByProjectId(p.getId()).stream())
                .collect(Collectors.toList());

        List<ActivityDTO> activities = new ArrayList<>();

        // Project creation activities
        projects.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .forEach(p -> {
                    User lead = (p.getTeamLeadId() != null)
                            ? userRepository.findById(p.getTeamLeadId()).orElse(null)
                            : null;
                    ActivityDTO.AssigneeInfo assigneeInfo = (lead != null)
                            ? new ActivityDTO.AssigneeInfo(lead.getName())
                            : null;
                    activities.add(new ActivityDTO(
                            p.getId(),
                            p.getName(),
                            "PLANNING",
                            "FEATURE",   // treat project creation as FEATURE activity type
                            assigneeInfo,
                            p.getCreatedAt()
                    ));
                });

        // Task update activities
        allTasks.stream()
                .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                .limit(10)
                .forEach(t -> {
                    User assignee = (t.getAssigneeId() != null)
                            ? userRepository.findById(t.getAssigneeId()).orElse(null)
                            : null;
                    ActivityDTO.AssigneeInfo assigneeInfo = (assignee != null)
                            ? new ActivityDTO.AssigneeInfo(assignee.getName())
                            : null;
                    activities.add(new ActivityDTO(
                            t.getId(),
                            t.getTitle(),
                            t.getStatus(),
                            t.getType(),
                            assigneeInfo,
                            t.getUpdatedAt()
                    ));
                });

        return activities.stream()
                .sorted((a, b) -> b.updatedAt.compareTo(a.updatedAt))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Get user's task summary.
     * Returns a single object shaped for TasksSummary.jsx:
     *   { myTasks: [], overdueTasks: [], inProgressTasks: [] }
     * Each item: { id, title, type, priority }
     */
    public TasksSummaryResponseDTO getUserTaskSummary(UUID workspaceId) {
        validateWorkspaceAccess(workspaceId);
        UUID userId = TenantContext.getCurrentUserIdRequired();

        List<Task> userTasks = taskRepository.findByAssigneeIdAndProjectWorkspaceId(userId, workspaceId);

        LocalDateTime now = LocalDateTime.now();

        List<TasksSummaryResponseDTO.TaskItem> myTasks = userTasks.stream()
                .map(t -> new TasksSummaryResponseDTO.TaskItem(t.getId(), t.getTitle(), t.getType(), t.getPriority()))
                .collect(Collectors.toList());

        List<TasksSummaryResponseDTO.TaskItem> overdueTasks = userTasks.stream()
                .filter(t -> t.getDueDate() != null
                        && t.getDueDate().isBefore(now)
                        && !"COMPLETED".equals(t.getStatus())
                        && !"DONE".equals(t.getStatus()))
                .map(t -> new TasksSummaryResponseDTO.TaskItem(t.getId(), t.getTitle(), t.getType(), t.getPriority()))
                .collect(Collectors.toList());

        List<TasksSummaryResponseDTO.TaskItem> inProgressTasks = userTasks.stream()
                .filter(t -> "IN_PROGRESS".equals(t.getStatus()))
                .map(t -> new TasksSummaryResponseDTO.TaskItem(t.getId(), t.getTitle(), t.getType(), t.getPriority()))
                .collect(Collectors.toList());

        return new TasksSummaryResponseDTO(myTasks, overdueTasks, inProgressTasks);
    }

    /**
     * Get project overview (active/planning projects only).
     * Returns fields ProjectOverview.jsx expects:
     *   id, name, status, progress, description, priority,
     *   end_date, members (empty list — members loaded separately if needed)
     */
    public List<ProjectOverviewDTO> getProjectOverview(UUID workspaceId) {
        validateWorkspaceAccess(workspaceId);

        return projectRepository.findByWorkspaceId(workspaceId).stream()
                .filter(p -> !("COMPLETED".equals(p.getStatus()) || "CANCELLED".equals(p.getStatus())))
                .map(p -> {
                    List<Task> tasks = taskRepository.findByProjectId(p.getId());
                    long completedTasks = tasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
                    User lead = (p.getTeamLeadId() != null)
                            ? userRepository.findById(p.getTeamLeadId()).orElse(null)
                            : null;

                    return new ProjectOverviewDTO(
                            p.getId(),
                            p.getName(),
                            p.getStatus(),
                            p.getProgress(),
                            p.getDescription(),
                            p.getPriority(),
                            (int) completedTasks,
                            tasks.size(),
                            lead != null ? lead.getName() : null,
                            p.getEndDate()
                    );
                })
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private void validateWorkspaceAccess(UUID workspaceId) {
        UUID userId = TenantContext.getCurrentUserIdRequired();
        boolean hasAccess = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId).isPresent();
        if (!hasAccess) {
            throw new IllegalArgumentException("Access denied to workspace");
        }
    }

    // =========================================================================
    // DTOs
    // =========================================================================

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
        // Fields the frontend StatsGrid specifically reads
        public int myTasks;
        public int overdueIssues;

        public DashboardStatsDTO(int totalProjects, int activeProjects, int completedProjects, int planningProjects,
                                  int totalTasks, int completedTasks, int inProgressTasks, int todoTasks,
                                  int teamMembers, int averageProgress, int myTasks, int overdueIssues) {
            this.totalProjects    = totalProjects;
            this.activeProjects   = activeProjects;
            this.completedProjects = completedProjects;
            this.planningProjects  = planningProjects;
            this.totalTasks       = totalTasks;
            this.completedTasks   = completedTasks;
            this.inProgressTasks  = inProgressTasks;
            this.todoTasks        = todoTasks;
            this.teamMembers      = teamMembers;
            this.averageProgress  = averageProgress;
            this.myTasks          = myTasks;
            this.overdueIssues    = overdueIssues;
        }

        public int getTotalProjects()     { return totalProjects; }
        public int getActiveProjects()    { return activeProjects; }
        public int getCompletedProjects() { return completedProjects; }
        public int getPlanningProjects()  { return planningProjects; }
        public int getTotalTasks()        { return totalTasks; }
        public int getCompletedTasks()    { return completedTasks; }
        public int getInProgressTasks()   { return inProgressTasks; }
        public int getTodoTasks()         { return todoTasks; }
        public int getTeamMembers()       { return teamMembers; }
        public int getAverageProgress()   { return averageProgress; }
        public int getMyTasks()           { return myTasks; }
        public int getOverdueIssues()     { return overdueIssues; }
    }

    /** Shaped for RecentActivity.jsx: id, title, status, type, updatedAt, assignee */
    public static class ActivityDTO {
        public UUID   id;
        public String title;
        public String status;
        public String type;
        public AssigneeInfo assignee;
        public LocalDateTime updatedAt;

        public ActivityDTO(UUID id, String title, String status, String type,
                           AssigneeInfo assignee, LocalDateTime updatedAt) {
            this.id       = id;
            this.title    = title;
            this.status   = status;
            this.type     = type;
            this.assignee = assignee;
            this.updatedAt = updatedAt;
        }

        public UUID   getId()       { return id; }
        public String getTitle()    { return title; }
        public String getStatus()   { return status; }
        public String getType()     { return type; }
        public AssigneeInfo getAssignee() { return assignee; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }

        public static class AssigneeInfo {
            public String fullName;
            public AssigneeInfo(String fullName) { this.fullName = fullName; }
            public String getFullName() { return fullName; }
        }
    }

    /** Shaped for TasksSummary.jsx: { myTasks[], overdueTasks[], inProgressTasks[] } */
    public static class TasksSummaryResponseDTO {
        public List<TaskItem> myTasks;
        public List<TaskItem> overdueTasks;
        public List<TaskItem> inProgressTasks;

        public TasksSummaryResponseDTO(List<TaskItem> myTasks, List<TaskItem> overdueTasks, List<TaskItem> inProgressTasks) {
            this.myTasks         = myTasks;
            this.overdueTasks    = overdueTasks;
            this.inProgressTasks = inProgressTasks;
        }

        public List<TaskItem> getMyTasks()         { return myTasks; }
        public List<TaskItem> getOverdueTasks()    { return overdueTasks; }
        public List<TaskItem> getInProgressTasks() { return inProgressTasks; }

        public static class TaskItem {
            public UUID   id;
            public String title;
            public String type;
            public String priority;

            public TaskItem(UUID id, String title, String type, String priority) {
                this.id       = id;
                this.title    = title;
                this.type     = type;
                this.priority = priority;
            }

            public UUID   getId()       { return id; }
            public String getTitle()    { return title; }
            public String getType()     { return type; }
            public String getPriority() { return priority; }
        }
    }

    /** Shaped for ProjectOverview.jsx: includes description, priority, end_date alias */
    public static class ProjectOverviewDTO {
        public UUID   id;
        public String name;
        public String status;
        public int    progress;
        public String description;
        public String priority;
        public int    completedTasks;
        public int    totalTasks;
        public String teamLead;
        // Exposed as both camelCase and snake_case so frontend works either way
        public LocalDateTime endDate;
        public LocalDateTime end_date;

        public ProjectOverviewDTO(UUID id, String name, String status, int progress,
                                   String description, String priority,
                                   int completedTasks, int totalTasks,
                                   String teamLead, LocalDateTime endDate) {
            this.id             = id;
            this.name           = name;
            this.status         = status;
            this.progress       = progress;
            this.description    = description;
            this.priority       = priority;
            this.completedTasks = completedTasks;
            this.totalTasks     = totalTasks;
            this.teamLead       = teamLead;
            this.endDate        = endDate;
            this.end_date       = endDate;
        }

        public UUID   getId()             { return id; }
        public String getName()           { return name; }
        public String getStatus()         { return status; }
        public int    getProgress()       { return progress; }
        public String getDescription()    { return description; }
        public String getPriority()       { return priority; }
        public int    getCompletedTasks() { return completedTasks; }
        public int    getTotalTasks()     { return totalTasks; }
        public String getTeamLead()       { return teamLead; }
        public LocalDateTime getEndDate() { return endDate; }
        public LocalDateTime getEnd_date(){ return end_date; }
    }

    // Keep for backward compatibility — not used by dashboard but may be elsewhere
    public static class UserTaskSummaryDTO {
        public String status;
        public int count;
        public List<TaskSummary> tasks;

        public UserTaskSummaryDTO(String status, int count, List<TaskSummary> tasks) {
            this.status = status;
            this.count  = count;
            this.tasks  = tasks;
        }

        public static class TaskSummary {
            public UUID   id;
            public String title;
            public String priority;

            public TaskSummary(UUID id, String title, String priority) {
                this.id       = id;
                this.title    = title;
                this.priority = priority;
            }

            public UUID   getId()       { return id; }
            public String getTitle()    { return title; }
            public String getPriority() { return priority; }
        }

        public String         getStatus() { return status; }
        public int            getCount()  { return count; }
        public List<TaskSummary> getTasks() { return tasks; }
    }
}
