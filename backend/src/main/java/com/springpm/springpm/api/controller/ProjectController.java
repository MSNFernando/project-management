package com.springpm.springpm.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.springpm.springpm.service.ProjectService;
import com.springpm.springpm.util.ApiResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/workspaces/{workspaceId}/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * List all projects in workspace
     * GET /api/workspaces/{workspaceId}/projects
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectService.ProjectDTO>>> listProjects(@PathVariable UUID workspaceId) {
        try {
            List<ProjectService.ProjectDTO> projects = projectService.listProjects(workspaceId);
            return ResponseEntity.ok(ApiResponse.success(projects));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), "ACCESS_DENIED"));
        }
    }

    /**
     * Get project by ID
     * GET /api/workspaces/{workspaceId}/projects/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectService.ProjectDTO>> getProject(@PathVariable UUID workspaceId, @PathVariable UUID id) {
        try {
            ProjectService.ProjectDTO project = projectService.getProject(workspaceId, id);
            return ResponseEntity.ok(ApiResponse.success(project));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "PROJECT_NOT_FOUND"));
        }
    }

    /**
     * Create new project
     * POST /api/workspaces/{workspaceId}/projects
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectService.ProjectDTO>> createProject(
            @PathVariable UUID workspaceId,
            @RequestBody CreateProjectRequest request) {
        try {
            ProjectService.ProjectDTO project = projectService.createProject(
                    workspaceId,
                    request.name,
                    request.description,
                    request.status,
                    request.priority,
                    request.startDate,
                    request.endDate
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(project));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "CREATE_PROJECT_FAILED"));
        }
    }

    /**
     * Update project
     * PUT /api/workspaces/{workspaceId}/projects/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectService.ProjectDTO>> updateProject(
            @PathVariable UUID workspaceId,
            @PathVariable UUID id,
            @RequestBody UpdateProjectRequest request) {
        try {
            ProjectService.ProjectDTO project = projectService.updateProject(
                    workspaceId,
                    id,
                    request.name,
                    request.description,
                    request.status,
                    request.priority,
                    request.progress,
                    request.startDate,
                    request.endDate
            );
            return ResponseEntity.ok(ApiResponse.success(project));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "UPDATE_PROJECT_FAILED"));
        }
    }

    /**
     * Delete project
     * DELETE /api/workspaces/{workspaceId}/projects/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable UUID workspaceId, @PathVariable UUID id) {
        try {
            projectService.deleteProject(workspaceId, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "DELETE_PROJECT_FAILED"));
        }
    }

    /**
     * Get project members
     * GET /api/workspaces/{workspaceId}/projects/{id}/members
     */
    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<ProjectService.ProjectMemberDTO>>> getProjectMembers(
            @PathVariable UUID workspaceId,
            @PathVariable UUID id) {
        try {
            List<ProjectService.ProjectMemberDTO> members = projectService.getProjectMembers(workspaceId, id);
            return ResponseEntity.ok(ApiResponse.success(members));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "PROJECT_NOT_FOUND"));
        }
    }

    /**
     * Add member to project
     * POST /api/workspaces/{workspaceId}/projects/{id}/members
     */
    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<ProjectService.ProjectMemberDTO>> addMemberToProject(
            @PathVariable UUID workspaceId,
            @PathVariable UUID id,
            @RequestBody AddProjectMemberRequest request) {
        try {
            ProjectService.ProjectMemberDTO member = projectService.addMemberToProject(workspaceId, id, request.userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(member));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "ADD_MEMBER_FAILED"));
        }
    }

    /**
     * Remove member from project
     * DELETE /api/workspaces/{workspaceId}/projects/{id}/members/{userId}
     */
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMemberFromProject(
            @PathVariable UUID workspaceId,
            @PathVariable UUID id,
            @PathVariable UUID userId) {
        try {
            projectService.removeMemberFromProject(workspaceId, id, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "REMOVE_MEMBER_FAILED"));
        }
    }

    /**
     * Get project analytics
     * GET /api/workspaces/{workspaceId}/projects/{id}/analytics
     */
    @GetMapping("/{id}/analytics")
    public ResponseEntity<ApiResponse<ProjectService.ProjectAnalyticsDTO>> getProjectAnalytics(
            @PathVariable UUID workspaceId,
            @PathVariable UUID id) {
        try {
            ProjectService.ProjectAnalyticsDTO analytics = projectService.getProjectAnalytics(workspaceId, id);
            return ResponseEntity.ok(ApiResponse.success(analytics));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "PROJECT_NOT_FOUND"));
        }
    }

    // Request DTOs
    @lombok.Data
    public static class CreateProjectRequest {
        private String name;
        private String description;
        private String status;
        private String priority;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @lombok.Data
    public static class UpdateProjectRequest {
        private String name;
        private String description;
        private String status;
        private String priority;
        private Integer progress;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @lombok.Data
    public static class AddProjectMemberRequest {
        private UUID userId;
    }
}
