package com.springpm.springpm.api.controller;

import com.springpm.springpm.service.TaskService;
import com.springpm.springpm.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Tasks across every project in a workspace that are assigned to the current user.
 * Used by the sidebar's My Tasks.
 */
@RestController
@RequestMapping("/workspaces/{workspaceId}/tasks")
public class AssignedTaskController {

    private final TaskService taskService;

    public AssignedTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * GET /workspaces/{workspaceId}/tasks/assigned
     */
    @GetMapping("/assigned")
    public ResponseEntity<ApiResponse<List<TaskService.TaskDTO>>> getAssignedTasks(@PathVariable UUID workspaceId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(taskService.getAssignedTasks(workspaceId)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), "ACCESS_DENIED"));
        }
    }
}
