package com.springpm.springpm.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.springpm.springpm.service.TaskService;
import com.springpm.springpm.util.ApiResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/workspaces/{workspaceId}/projects/{projectId}/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * List all tasks in project
     * GET /api/workspaces/{workspaceId}/projects/{projectId}/tasks
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskService.TaskDTO>>> listTasks(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId) {
        try {
            List<TaskService.TaskDTO> tasks = taskService.listTasks(workspaceId, projectId);
            return ResponseEntity.ok(ApiResponse.success(tasks));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), "ACCESS_DENIED"));
        }
    }

    /**
     * Get task by ID
     * GET /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskService.TaskDTO>> getTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID id) {
        try {
            TaskService.TaskDTO task = taskService.getTask(workspaceId, projectId, id);
            return ResponseEntity.ok(ApiResponse.success(task));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "TASK_NOT_FOUND"));
        }
    }

    /**
     * Create new task
     * POST /api/workspaces/{workspaceId}/projects/{projectId}/tasks
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TaskService.TaskDTO>> createTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @RequestBody CreateTaskRequest request) {
        try {
            TaskService.TaskDTO task = taskService.createTask(
                    workspaceId,
                    projectId,
                    request.title,
                    request.description,
                    request.type,
                    request.status,
                    request.priority,
                    request.assigneeId,
                    request.dueDate
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(task));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "CREATE_TASK_FAILED"));
        }
    }

    /**
     * Update task
     * PUT /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskService.TaskDTO>> updateTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID id,
            @RequestBody UpdateTaskRequest request) {
        try {
            TaskService.TaskDTO task = taskService.updateTask(
                    workspaceId,
                    projectId,
                    id,
                    request.title,
                    request.description,
                    request.type,
                    request.status,
                    request.priority,
                    request.assigneeId,
                    request.dueDate
            );
            return ResponseEntity.ok(ApiResponse.success(task));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "UPDATE_TASK_FAILED"));
        }
    }

    /**
     * Delete task
     * DELETE /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID id) {
        try {
            taskService.deleteTask(workspaceId, projectId, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "DELETE_TASK_FAILED"));
        }
    }

    /**
     * Add comment to task
     * POST /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{id}/comments
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<TaskService.TaskCommentDTO>> addComment(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID id,
            @RequestBody AddCommentRequest request) {
        try {
            TaskService.TaskCommentDTO comment = taskService.addComment(
                    workspaceId,
                    projectId,
                    id,
                    request.content
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(comment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "ADD_COMMENT_FAILED"));
        }
    }

    /**
     * Get task comments
     * GET /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{id}/comments
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<TaskService.TaskCommentDTO>>> getTaskComments(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID id) {
        try {
            List<TaskService.TaskCommentDTO> comments = taskService.getTaskComments(workspaceId, projectId, id);
            return ResponseEntity.ok(ApiResponse.success(comments));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), "TASK_NOT_FOUND"));
        }
    }

    /**
     * Delete comment
     * DELETE /api/workspaces/{workspaceId}/projects/{projectId}/tasks/{id}/comments/{commentId}
     */
    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID id,
            @PathVariable UUID commentId) {
        try {
            taskService.deleteComment(workspaceId, projectId, id, commentId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "DELETE_COMMENT_FAILED"));
        }
    }

    // Request DTOs
    @lombok.Data
    public static class CreateTaskRequest {
        private String title;
        private String description;
        private String type;
        private String status;
        private String priority;
        private UUID assigneeId;
        private LocalDateTime dueDate;
    }

    @lombok.Data
    public static class UpdateTaskRequest {
        private String title;
        private String description;
        private String type;
        private String status;
        private String priority;
        private UUID assigneeId;
        private LocalDateTime dueDate;
    }

    @lombok.Data
    public static class AddCommentRequest {
        private String content;
    }
}
