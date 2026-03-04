package com.springpm.springpm.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.springpm.springpm.service.WorkspaceService;
import com.springpm.springpm.util.ApiResponse;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/workspaces")
@CrossOrigin(origins = "*")
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * Get all workspaces for current user
     * GET /api/workspaces
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkspaceService.WorkspaceDTO>>> getAllWorkspaces() {
        try {
            List<WorkspaceService.WorkspaceDTO> workspaces = workspaceService.getAllWorkspacesForCurrentUser();
            return ResponseEntity.ok(ApiResponse.success(workspaces));
        } catch (Exception e) {
            log.error("Error fetching workspaces", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), "FETCH_WORKSPACES_FAILED"));
        }
    }

    /**
     * Get workspace by ID
     * GET /api/workspaces/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkspaceService.WorkspaceDTO>> getWorkspace(@PathVariable UUID id) {
        try {
            WorkspaceService.WorkspaceDTO workspace = workspaceService.getWorkspace(id);
            return ResponseEntity.ok(ApiResponse.success(workspace));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "WORKSPACE_NOT_FOUND"));
        }
    }

    /**
     * Create new workspace
     * POST /api/workspaces
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WorkspaceService.WorkspaceDTO>> createWorkspace(@RequestBody CreateWorkspaceRequest request) {
        try {
            WorkspaceService.WorkspaceDTO workspace = workspaceService.createWorkspace(request.name, request.description);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(workspace));
        } catch (Exception e) {
            log.error("Error creating workspace", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "CREATE_WORKSPACE_FAILED"));
        }
    }

    /**
     * Update workspace
     * PUT /api/workspaces/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkspaceService.WorkspaceDTO>> updateWorkspace(@PathVariable UUID id, @RequestBody UpdateWorkspaceRequest request) {
        try {
            WorkspaceService.WorkspaceDTO workspace = workspaceService.updateWorkspace(id, request.name, request.description);
            return ResponseEntity.ok(ApiResponse.success(workspace));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "UPDATE_WORKSPACE_FAILED"));
        }
    }

    /**
     * Delete workspace
     * DELETE /api/workspaces/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(@PathVariable UUID id) {
        try {
            workspaceService.deleteWorkspace(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "DELETE_WORKSPACE_FAILED"));
        }
    }

    /**
     * Get workspace members
     * GET /api/workspaces/{id}/members
     */
    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<WorkspaceService.WorkspaceMemberDTO>>> getWorkspaceMembers(@PathVariable UUID id) {
        try {
            List<WorkspaceService.WorkspaceMemberDTO> members = workspaceService.getWorkspaceMembers(id);
            return ResponseEntity.ok(ApiResponse.success(members));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "WORKSPACE_NOT_FOUND"));
        }
    }

    /**
     * Invite user to workspace
     * POST /api/workspaces/{id}/members/invite
     */
    @PostMapping("/{id}/members/invite")
    public ResponseEntity<ApiResponse<WorkspaceService.WorkspaceMemberDTO>> inviteUserToWorkspace(
            @PathVariable UUID id,
            @RequestBody InviteUserRequest request) {
        try {
            WorkspaceService.WorkspaceMemberDTO member = workspaceService.inviteUserToWorkspace(id, request.email, request.role);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(member));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "INVITE_FAILED"));
        }
    }

    /**
     * Update member role
     * PUT /api/workspaces/{id}/members/{userId}
     */
    @PutMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<WorkspaceService.WorkspaceMemberDTO>> updateMemberRole(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @RequestBody UpdateMemberRoleRequest request) {
        try {
            WorkspaceService.WorkspaceMemberDTO member = workspaceService.updateMemberRole(id, userId, request.role);
            return ResponseEntity.ok(ApiResponse.success(member));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "UPDATE_MEMBER_FAILED"));
        }
    }

    /**
     * Remove member from workspace
     * DELETE /api/workspaces/{id}/members/{userId}
     */
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMemberFromWorkspace(
            @PathVariable UUID id,
            @PathVariable UUID userId) {
        try {
            workspaceService.removeMemberFromWorkspace(id, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "REMOVE_MEMBER_FAILED"));
        }
    }

    // Request DTOs
    @lombok.Data
    public static class CreateWorkspaceRequest {
        private String name;
        private String description;
    }

    @lombok.Data
    public static class UpdateWorkspaceRequest {
        private String name;
        private String description;
    }

    @lombok.Data
    public static class InviteUserRequest {
        private String email;
        private String role;
    }

    @lombok.Data
    public static class UpdateMemberRoleRequest {
        private String role;
    }
}
