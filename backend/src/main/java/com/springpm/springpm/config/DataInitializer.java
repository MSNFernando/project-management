package com.springpm.springpm.config;

import com.springpm.springpm.entity.User;
import com.springpm.springpm.entity.Workspace;
import com.springpm.springpm.entity.Role;
import com.springpm.springpm.entity.WorkspaceMember;
import com.springpm.springpm.repository.UserRepository;
import com.springpm.springpm.repository.WorkspaceRepository;
import com.springpm.springpm.repository.RoleRepository;
import com.springpm.springpm.repository.WorkspaceMemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Initializes the database with default data on application startup.
 * Creates an admin user if one doesn't exist.
 */
@Slf4j
@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@springpm.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin1234!}")
    private String adminPassword;

    @Value("${app.admin.name:Admin User}")
    private String adminName;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try {
            initializeAdminUser();
        } catch (Exception e) {
            log.error("Error initializing database with default data", e);
        }
    }

    /**
     * Creates default admin user and workspace if they don't exist
     */
    private void initializeAdminUser() {
        // Check if admin user already exists
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user already exists");
            return;
        }

        log.info("Creating default admin user with email: {}", adminEmail);

        // Create admin user
        User adminUser = User.builder()
                .email(adminEmail)
                .name(adminName)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(adminUser);
        log.info("Admin user created with ID: {}", savedUser.getId());

        // Create default workspace for admin
        Workspace workspace = Workspace.builder()
                .name("Default Workspace")
                .slug("default-workspace")
                .description("Default workspace for admin user")
                .ownerId(savedUser.getId())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Workspace savedWorkspace = workspaceRepository.save(workspace);
        log.info("Default workspace created with ID: {}", savedWorkspace.getId());

        // Create default roles for workspace
        Role adminRole = Role.builder()
                .workspaceId(savedWorkspace.getId())
                .name("ADMIN")
                .description("Administrator with full access")
                .createdAt(LocalDateTime.now())
                .build();

        Role memberRole = Role.builder()
                .workspaceId(savedWorkspace.getId())
                .name("MEMBER")
                .description("Team member with standard access")
                .createdAt(LocalDateTime.now())
                .build();

        Role viewerRole = Role.builder()
                .workspaceId(savedWorkspace.getId())
                .name("VIEWER")
                .description("View-only access")
                .createdAt(LocalDateTime.now())
                .build();

        Role savedAdminRole = roleRepository.save(adminRole);
        roleRepository.save(memberRole);
        roleRepository.save(viewerRole);
        log.info("Default roles created for workspace");

        // Add admin user to workspace with ADMIN role
        WorkspaceMember workspaceMember = WorkspaceMember.builder()
                .workspaceId(savedWorkspace.getId())
                .userId(savedUser.getId())
                .role("ADMIN")
                .isActive(true)
                .joinedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        workspaceMemberRepository.save(workspaceMember);
        log.info("Admin user added to default workspace as ADMIN");
        log.info("Database initialization completed successfully");
    }
}
