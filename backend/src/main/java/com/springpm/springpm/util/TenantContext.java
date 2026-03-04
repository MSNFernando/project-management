package com.springpm.springpm.util;

import java.util.Optional;
import java.util.UUID;

/**
 * ThreadLocal context for managing current user and workspace during request processing
 */
public class TenantContext {
    private static final ThreadLocal<UUID> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<UUID> currentWorkspaceId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserEmail = new ThreadLocal<>();

    public static void setCurrentUserId(UUID userId) {
        currentUserId.set(userId);
    }

    public static Optional<UUID> getCurrentUserId() {
        return Optional.ofNullable(currentUserId.get());
    }

    public static UUID getCurrentUserIdRequired() {
        return getCurrentUserId().orElseThrow(() -> 
            new IllegalStateException("User ID not set in TenantContext"));
    }

    public static void setCurrentWorkspaceId(UUID workspaceId) {
        currentWorkspaceId.set(workspaceId);
    }

    public static Optional<UUID> getCurrentWorkspaceId() {
        return Optional.ofNullable(currentWorkspaceId.get());
    }

    public static UUID getCurrentWorkspaceIdRequired() {
        return getCurrentWorkspaceId().orElseThrow(() -> 
            new IllegalStateException("Workspace ID not set in TenantContext"));
    }

    public static void setCurrentUserEmail(String email) {
        currentUserEmail.set(email);
    }

    public static Optional<String> getCurrentUserEmail() {
        return Optional.ofNullable(currentUserEmail.get());
    }

    public static void clear() {
        currentUserId.remove();
        currentWorkspaceId.remove();
        currentUserEmail.remove();
    }
}
