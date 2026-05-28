package com.finalyear.liwatch.admin;

import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.utils.enums.Role;
import com.finalyear.liwatch.userManagement.utils.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserRepository userRepo;

    // ── List / search ──────────────────────────────────────────────────────────

    /**
     * Returns a paginated list of users.
     * Any combination of keyword / status / role may be null → acts as "no filter".
     */
    @Transactional(readOnly = true)
    public AdminApiResponse<java.util.List<AdminUserResponse>> listUsers(
            String keyword,
            String status,
            String role,
            Pageable pageable) {

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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Cannot suspend another admin account.");
        }

        user.setStatus(Status.SUSPENDED);
        user.setEnabled(false);
        userRepo.save(user);

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

        user.setRole(Role.USER);
        userRepo.save(user);

        log.info("[ADMIN] Demoted user {} to USER — reason: {}", userId, req.getReason());
        return AdminApiResponse.ok(AdminUserResponse.from(user),
                "User demoted to regular user.");
    }

    // ── Force-verify a user ───────────────────────────────────────────────────

    @Transactional
    public AdminApiResponse<AdminUserResponse> forceVerifyUser(Long userId) {
        User user = findOrThrow(userId);

        user.setVerified(true);
        user.setEnabled(true);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);
        userRepo.save(user);

        log.info("[ADMIN] Force-verified user {}", userId);
        return AdminApiResponse.ok(AdminUserResponse.from(user),
                "User verified and enabled.");
    }

    // ── Delete user ───────────────────────────────────────────────────────────

    @Transactional
    public AdminApiResponse<Void> deleteUser(Long userId, AdminActionRequest req) {
        User user = findOrThrow(userId);

        if (user.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Cannot delete an admin account.");
        }

        userRepo.delete(user);

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
