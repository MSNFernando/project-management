package com.springpm.springpm.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.springpm.springpm.service.DashboardService;
import com.springpm.springpm.util.ApiResponse;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/workspaces/{workspaceId}/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Get dashboard statistics
     * GET /api/workspaces/{workspaceId}/dashboard/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardService.DashboardStatsDTO>> getDashboardStats(
            @PathVariable UUID workspaceId) {
        try {
            DashboardService.DashboardStatsDTO stats = dashboardService.getDashboardStats(workspaceId);
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), "ACCESS_DENIED"));
        }
    }

    /**
     * Get recent activity
     * GET /api/workspaces/{workspaceId}/dashboard/recent-activity
     */
    @GetMapping("/recent-activity")
    public ResponseEntity<ApiResponse<List<DashboardService.ActivityDTO>>> getRecentActivity(
            @PathVariable UUID workspaceId) {
        try {
            List<DashboardService.ActivityDTO> activities = dashboardService.getRecentActivity(workspaceId);
            return ResponseEntity.ok(ApiResponse.success(activities));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), "ACCESS_DENIED"));
        }
    }

    /**
     * Get user's task summary
     * GET /api/workspaces/{workspaceId}/dashboard/tasks-summary
     */
    @GetMapping("/tasks-summary")
    public ResponseEntity<ApiResponse<List<DashboardService.UserTaskSummaryDTO>>> getUserTaskSummary(
            @PathVariable UUID workspaceId) {
        try {
            List<DashboardService.UserTaskSummaryDTO> summary = dashboardService.getUserTaskSummary(workspaceId);
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), "ACCESS_DENIED"));
        }
    }

    /**
     * Get project overview (ongoing projects)
     * GET /api/workspaces/{workspaceId}/dashboard/project-overview
     */
    @GetMapping("/project-overview")
    public ResponseEntity<ApiResponse<List<DashboardService.ProjectOverviewDTO>>> getProjectOverview(
            @PathVariable UUID workspaceId) {
        try {
            List<DashboardService.ProjectOverviewDTO> overview = dashboardService.getProjectOverview(workspaceId);
            return ResponseEntity.ok(ApiResponse.success(overview));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), "ACCESS_DENIED"));
        }
    }
}
