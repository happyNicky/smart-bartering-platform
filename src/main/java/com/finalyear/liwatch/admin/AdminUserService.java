package com.finalyear.liwatch.admin;

import com.finalyear.liwatch.Notification.NotificationService;
import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.utils.enums.Role;
import com.finalyear.liwatch.userManagement.utils.enums.Status;
import com.finalyear.liwatch.userManagement.utils.classes.UserUtilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserRepository userRepo;
    private final AdminActionLogRepository logRepo;
    private final UserUtilService userUtil;
    private final NotificationService notificationService;

    // ── List / search ──────────────────────────────────────────────────────────

    /**
     * Returns a paginated list of users.
     * Any combination of keyword / status / role may be null → acts as "no filter".
     */
    @Transactional(readOnly = true)
    public AdminApiResponse<java.util.List<AdminUserResponse>> listUsers(
            String keyword,
            String statusStr,
            String roleStr,
            Pageable pageable) {

        Status status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try { status = Status.valueOf(statusStr.toUpperCase()); } catch(Exception e){}
        }

        Role role = null;
        if (roleStr != null && !roleStr.isBlank()) {
            try { role = Role.valueOf(roleStr.toUpperCase()); } catch(Exception e){}
        }

        Page<User> page = userRepo.searchUsers(keyword, status, role, pageable);

        Page<AdminUserResponse> mapped = page.map(user -> {
            AdminUserResponse dto = AdminUserResponse.from(user);
            dto.setTotalPosts(userRepo.countPostsByUserId(user.getId()));
            dto.setTotalBarters(userRepo.countBartersByUserId(user.getId()));
            dto.setReportCount(userRepo.countReportsByUserId(user.getId()));
            return dto;
        });

        return AdminApiResponse.ofPage(mapped);
    }

    // ── Single user detail ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminApiResponse<AdminUserResponse> getUserDetail(Long userId) {
        User user = findOrThrow(userId);

        AdminUserResponse dto = AdminUserResponse.from(user);
        dto.setTotalPosts(userRepo.countPostsByUserId(userId));
        dto.setTotalBarters(userRepo.countBartersByUserId(userId));
        dto.setReportCount(userRepo.countReportsByUserId(userId));

        return AdminApiResponse.ok(dto);
    }

    // ── Suspend user ──────────────────────────────────────────────────────────

    @Transactional
    public AdminApiResponse<AdminUserResponse> suspendUser(Long userId, AdminActionRequest req) {
        User user = findOrThrow(userId);

        if (user.getStatus() == Status.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "User is already suspended.");
        }
        if (user.getRole() == Role.ADMIN) {
            if (user.getId().equals(userUtil.getCurrentlyAuthenticatedUser().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You cannot suspend your own admin account.");
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Cannot suspend another admin account.");
        }

        user.setStatus(Status.SUSPENDED);
        user.setEnabled(false);
        userRepo.save(user);

        try {
            String adminEmail = userUtil.getCurrentlyAuthenticatedUser().getEmail();
            logRepo.save(AdminActionLog.builder()
                    .adminEmail(adminEmail)
                    .actionType("SUSPEND_USER")
                    .targetType("USER")
                    .targetId(userId)
                    .reason(req.getReason())
                    .actionTime(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to save admin action log: {}", e.getMessage());
        }

        try {
            notificationService.createNotification(
                    userId,
                    user.getEmail(),
                    "Account Suspended",
                    "Your account has been suspended by the administrator. Reason: " + req.getReason(),
                    "ACCOUNT_SUSPENDED"
            );
        } catch (Exception e) {
            log.error("Failed to send suspension notification to user {}: {}", userId, e.getMessage());
        }

        log.info("[ADMIN] Suspended user {} — reason: {}", userId, req.getReason());
        return AdminApiResponse.ok(AdminUserResponse.from(user),
                "User suspended successfully.");
    }

    // ── Activate user ─────────────────────────────────────────────────────────

    @Transactional
    public AdminApiResponse<AdminUserResponse> activateUser(Long userId, AdminActionRequest req) {
        User user = findOrThrow(userId);

        if (user.getStatus() == Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "User is already active.");
        }

        user.setStatus(Status.ACTIVE);
        user.setEnabled(true);
        userRepo.save(user);

        try {
            String adminEmail = userUtil.getCurrentlyAuthenticatedUser().getEmail();
            logRepo.save(AdminActionLog.builder()
                    .adminEmail(adminEmail)
                    .actionType("ACTIVATE_USER")
                    .targetType("USER")
                    .targetId(userId)
                    .reason(req.getReason())
                    .actionTime(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to save admin action log: {}", e.getMessage());
        }

        try {
            notificationService.createNotification(
                    userId,
                    user.getEmail(),
                    "Account Activated",
                    "Your account has been activated/unsuspended by the administrator.",
                    "ACCOUNT_ACTIVATED"
            );
        } catch (Exception e) {
            log.error("Failed to send activation notification to user {}: {}", userId, e.getMessage());
        }

        log.info("[ADMIN] Activated user {} — reason: {}", userId, req.getReason());
        return AdminApiResponse.ok(AdminUserResponse.from(user),
                "User activated successfully.");
    }

    // ── Promote to admin ──────────────────────────────────────────────────────

    @Transactional
    public AdminApiResponse<AdminUserResponse> promoteToAdmin(Long userId, AdminActionRequest req) {
        User user = findOrThrow(userId);

        if (user.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "User is already an admin.");
        }

        user.setRole(Role.ADMIN);
        userRepo.save(user);

        try {
            String adminEmail = userUtil.getCurrentlyAuthenticatedUser().getEmail();
            logRepo.save(AdminActionLog.builder()
                    .adminEmail(adminEmail)
                    .actionType("PROMOTE_USER")
                    .targetType("USER")
                    .targetId(userId)
                    .reason(req.getReason())
                    .actionTime(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to save admin action log: {}", e.getMessage());
        }

        try {
            notificationService.createNotification(
                    userId,
                    user.getEmail(),
                    "Role Updated",
                    "You have been promoted to administrator.",
                    "ROLE_UPDATED"
            );
        } catch (Exception e) {
            log.error("Failed to send promotion notification to user {}: {}", userId, e.getMessage());
        }

        log.info("[ADMIN] Promoted user {} to ADMIN — reason: {}", userId, req.getReason());
        return AdminApiResponse.ok(AdminUserResponse.from(user),
                "User promoted to admin.");
    }

    // ── Demote to regular user ────────────────────────────────────────────────

    @Transactional
    public AdminApiResponse<AdminUserResponse> demoteToUser(Long userId, AdminActionRequest req) {
        User user = findOrThrow(userId);

        if (user.getRole() == Role.USER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "User already has the USER role.");
        }

        User currentAdmin = userUtil.getCurrentlyAuthenticatedUser();
        if (currentAdmin.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You cannot demote your own admin account.");
        }

        user.setRole(Role.USER);
        userRepo.save(user);

        try {
            String adminEmail = userUtil.getCurrentlyAuthenticatedUser().getEmail();
            logRepo.save(AdminActionLog.builder()
                    .adminEmail(adminEmail)
                    .actionType("DEMOTE_USER")
                    .targetType("USER")
                    .targetId(userId)
                    .reason(req.getReason())
                    .actionTime(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to save admin action log: {}", e.getMessage());
        }

        try {
            notificationService.createNotification(
                    userId,
                    user.getEmail(),
                    "Role Updated",
                    "Your role has been updated to regular user.",
                    "ROLE_UPDATED"
            );
        } catch (Exception e) {
            log.error("Failed to send demotion notification to user {}: {}", userId, e.getMessage());
        }

        log.info("[ADMIN] Demoted user {} to USER — reason: {}", userId, req.getReason());
        return AdminApiResponse.ok(AdminUserResponse.from(user),
                "User demoted to regular user.");
    }


    // ── Delete user ───────────────────────────────────────────────────────────

    @Transactional
    public AdminApiResponse<Void> deleteUser(Long userId, AdminActionRequest req) {
        User user = findOrThrow(userId);

        if (user.getRole() == Role.ADMIN) {
            if (user.getId().equals(userUtil.getCurrentlyAuthenticatedUser().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You cannot delete your own admin account.");
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Cannot delete an admin account.");
        }

        userRepo.delete(user);

        try {
            String adminEmail = userUtil.getCurrentlyAuthenticatedUser().getEmail();
            logRepo.save(AdminActionLog.builder()
                    .adminEmail(adminEmail)
                    .actionType("DELETE_USER")
                    .targetType("USER")
                    .targetId(userId)
                    .reason(req.getReason())
                    .actionTime(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to save admin action log: {}", e.getMessage());
        }

        log.warn("[ADMIN] DELETED user {} ({}) — reason: {}",
                userId, user.getEmail(), req.getReason());
        return AdminApiResponse.ok(null, "User deleted permanently.");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private User findOrThrow(Long userId) {
        return userRepo.findByIdWithProfile(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: id=" + userId));
    }
}
