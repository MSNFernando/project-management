package com.springpm.springpm.service;

import com.springpm.springpm.entity.Role;
import com.springpm.springpm.entity.Workspace;
import com.springpm.springpm.entity.WorkspaceMember;
import com.springpm.springpm.entity.User;
import com.springpm.springpm.repository.RoleRepository;
import com.springpm.springpm.repository.WorkspaceRepository;
import com.springpm.springpm.repository.WorkspaceMemberRepository;
import com.springpm.springpm.repository.UserRepository;
import com.springpm.springpm.util.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /** Roles every workspace has, with their descriptions. */
    private static final Map<String, String> DEFAULT_ROLES = new LinkedHashMap<>();
    static {
        DEFAULT_ROLES.put("ADMIN", "Administrator with full access");
        DEFAULT_ROLES.put("MEMBER", "Team member with standard access");
        DEFAULT_ROLES.put("VIEWER", "View-only access");
    }

    public WorkspaceService(WorkspaceRepository workspaceRepository,
                           WorkspaceMemberRepository workspaceMemberRepository,
                           UserRepository userRepository,
                           RoleRepository roleRepository) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Get all workspaces for current user
     */
    public List<WorkspaceDTO> getAllWorkspacesForCurrentUser() {
        UUID userId = TenantContext.getCurrentUserIdRequired();
        List<Workspace> workspaces = workspaceRepository.findByOwnerId(userId);
        workspaces.addAll(workspaceRepository.findByMemberId(userId));

        return workspaces.stream()
                .distinct()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get workspace by ID with full details
     */
    public WorkspaceDTO getWorkspace(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));

        // Verify access
        validateWorkspaceAccess(workspaceId);

        return convertToDTO(workspace);
    }

    /**
     * Create new workspace
     */
    public WorkspaceDTO createWorkspace(String name, String description) {
        UUID userId = TenantContext.getCurrentUserIdRequired();
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Workspace name is required");
        }

        Workspace workspace = new Workspace();
        workspace.setName(name.trim());
        workspace.setSlug(uniqueSlug(name));
        workspace.setDescription(description);
        workspace.setOwnerId(userId);
        workspace.setCreatedAt(LocalDateTime.now());
        workspace.setUpdatedAt(LocalDateTime.now());
        workspace = workspaceRepository.save(workspace);

        // Every workspace gets ADMIN, MEMBER and VIEWER roles; the owner is ADMIN
        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspaceId(workspace.getId());
        member.setUserId(userId);
        member.setRole("ADMIN");
        member.setRoleId(roleFor(workspace, "ADMIN").getId());
        member.setJoinedAt(LocalDateTime.now());
        workspaceMemberRepository.save(member);

        return convertToDTO(workspace);
    }

    /**
     * Update workspace
     */
    public WorkspaceDTO updateWorkspace(UUID workspaceId, String name, String description) {
        validateWorkspaceAccess(workspaceId);
        validateIsWorkspaceAdmin(workspaceId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));

        workspace.setName(name);
        workspace.setDescription(description);
        workspace.setUpdatedAt(LocalDateTime.now());
        workspace = workspaceRepository.save(workspace);

        return convertToDTO(workspace);
    }

    /**
     * Delete workspace (owners only)
     */
    public void deleteWorkspace(UUID workspaceId) {
        validateWorkspaceAccess(workspaceId);
        UUID userId = TenantContext.getCurrentUserIdRequired();

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));

        if (!workspace.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Only workspace owner can delete");
        }

        workspaceMemberRepository.deleteByWorkspaceId(workspaceId);
        workspaceRepository.delete(workspace);
    }

    /**
     * Get workspace members
     */
    public List<WorkspaceMemberDTO> getWorkspaceMembers(UUID workspaceId) {
        validateWorkspaceAccess(workspaceId);

        return workspaceMemberRepository.findByWorkspaceId(workspaceId).stream()
                .map(member -> {
                    User user = userRepository.findById(member.getUserId())
                            .orElse(null);
                    return convertMemberToDTO(member, user);
                })
                .collect(Collectors.toList());
    }

    /**
     * Invite user to workspace
     */
    public WorkspaceMemberDTO inviteUserToWorkspace(UUID workspaceId, String email, String role) {
        validateWorkspaceAccess(workspaceId);
        validateIsWorkspaceAdmin(workspaceId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if already member
        if (workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, user.getId()).isPresent()) {
            throw new IllegalArgumentException("User already member of workspace");
        }

        String roleName = normaliseRole(role);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspaceId(workspaceId);
        member.setUserId(user.getId());
        member.setRole(roleName);
        member.setRoleId(roleFor(workspace, roleName).getId());
        member.setJoinedAt(LocalDateTime.now());
        member = workspaceMemberRepository.save(member);

        return convertMemberToDTO(member, user);
    }

    /**
     * Update user role in workspace
     */
    public WorkspaceMemberDTO updateMemberRole(UUID workspaceId, UUID userId, String role) {
        validateWorkspaceAccess(workspaceId);
        validateIsWorkspaceAdmin(workspaceId);

        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not member of workspace"));

        String roleName = normaliseRole(role);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
        member.setRole(roleName);
        member.setRoleId(roleFor(workspace, roleName).getId());
        member = workspaceMemberRepository.save(member);

        User user = userRepository.findById(userId).orElse(null);
        return convertMemberToDTO(member, user);
    }

    /**
     * Remove user from workspace
     */
    public void removeMemberFromWorkspace(UUID workspaceId, UUID userId) {
        validateWorkspaceAccess(workspaceId);
        validateIsWorkspaceAdmin(workspaceId);

        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not member of workspace"));

        workspaceMemberRepository.delete(member);
    }

    // Helper methods
    private void validateWorkspaceAccess(UUID workspaceId) {
        UUID userId = TenantContext.getCurrentUserIdRequired();
        boolean hasAccess = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId).isPresent()
                || workspaceRepository.findByIdAndOwnerId(workspaceId, userId).isPresent();

        if (!hasAccess) {
            throw new IllegalArgumentException("Access denied to workspace");
        }
    }

    private void validateIsWorkspaceAdmin(UUID workspaceId) {
        UUID userId = TenantContext.getCurrentUserIdRequired();
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User not member of workspace"));

        if (!"ADMIN".equals(member.getRole())) {
            throw new IllegalArgumentException("Only admins can perform this action");
        }
    }

    private String generateSlug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    /** Slugs are unique across all workspaces, so add -2, -3, … when one is taken. */
    private String uniqueSlug(String name) {
        String base = generateSlug(name);
        if (base.isEmpty()) {
            base = "workspace";
        }
        String slug = base;
        int n = 2;
        while (workspaceRepository.findBySlug(slug).isPresent()) {
            slug = base + "-" + n++;
        }
        return slug;
    }

    /** ADMIN, MEMBER or VIEWER; MEMBER when none is given. */
    private String normaliseRole(String role) {
        String name = (role == null || role.isBlank()) ? "MEMBER" : role.trim().toUpperCase(Locale.ROOT);
        if (!DEFAULT_ROLES.containsKey(name)) {
            throw new IllegalArgumentException("Role must be one of " + String.join(", ", DEFAULT_ROLES.keySet()));
        }
        return name;
    }

    /** The workspace's role with this name, creating the default roles if the workspace has none yet. */
    private Role roleFor(Workspace workspace, String roleName) {
        return roleRepository.findByWorkspaceIdAndName(workspace.getId(), roleName)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .workspace(workspace)
                        .name(roleName)
                        .description(DEFAULT_ROLES.get(roleName))
                        .createdAt(LocalDateTime.now())
                        .build()));
    }

    private WorkspaceDTO convertToDTO(Workspace workspace) {
        List<WorkspaceMember> members = workspaceMemberRepository.findByWorkspaceId(workspace.getId());
        return new WorkspaceDTO(
                workspace.getId(),
                workspace.getName(),
                workspace.getSlug(),
                workspace.getDescription(),
                workspace.getOwnerId(),
                members.size(),
                workspace.getCreatedAt(),
                workspace.getUpdatedAt()
        );
    }

    private WorkspaceMemberDTO convertMemberToDTO(WorkspaceMember member, User user) {
        return new WorkspaceMemberDTO(
                member.getId(),
                member.getWorkspaceId(),
                member.getUserId(),
                user != null ? user.getName() : "Unknown",
                user != null ? user.getEmail() : "Unknown",
                member.getRole(),
                member.getJoinedAt()
        );
    }

    // DTOs
    public static class WorkspaceDTO {
        public UUID id;
        public String name;
        public String slug;
        public String description;
        public UUID ownerId;
        public int memberCount;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;

        public WorkspaceDTO(UUID id, String name, String slug, String description, UUID ownerId, int memberCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.name = name;
            this.slug = slug;
            this.description = description;
            this.ownerId = ownerId;
            this.memberCount = memberCount;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        // Getters
        public UUID getId() { return id; }
        public String getName() { return name; }
        public String getSlug() { return slug; }
        public String getDescription() { return description; }
        public UUID getOwnerId() { return ownerId; }
        public int getMemberCount() { return memberCount; }
    }

    public static class WorkspaceMemberDTO {
        public UUID id;
        public UUID workspaceId;
        public UUID userId;
        public String userName;
        public String email;
        public String role;
        public LocalDateTime joinedAt;
        public UserInfo user;

        public WorkspaceMemberDTO(UUID id, UUID workspaceId, UUID userId, String userName, String email, String role, LocalDateTime joinedAt) {
            this.id = id;
            this.workspaceId = workspaceId;
            this.userId = userId;
            this.userName = userName;
            this.email = email;
            this.role = role;
            this.joinedAt = joinedAt;
            this.user = new UserInfo(userName, email);
        }

        // Getters
        public UUID getId() { return id; }
        public UUID getWorkspaceId() { return workspaceId; }
        public UUID getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public LocalDateTime getJoinedAt() { return joinedAt; }
        public UserInfo getUser() { return user; }

        public static class UserInfo {
            public String fullName;
            public String email;
            public UserInfo(String fullName, String email) {
                this.fullName = fullName;
                this.email = email;
            }
            public String getFullName() { return fullName; }
            public String getEmail() { return email; }
        }
    }
}
