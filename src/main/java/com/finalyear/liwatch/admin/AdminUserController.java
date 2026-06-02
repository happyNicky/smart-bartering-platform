package com.finalyear.liwatch.admin;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin endpoints for user management.
 *
 * Base path: /api/admin/users
 *
 * All endpoints require ROLE_ADMIN (enforced at both filter-chain
 * level via AdminSecurityConfig AND method level via @AdminOnly).
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@AdminOnly          // applies to every method in this controller
public class AdminUserController {

    private final AdminUserService userService;

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/admin/users
    //   ?keyword=john&status=ACTIVE&role=USER&page=0&size=20
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<AdminApiResponse<List<AdminUserResponse>>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(userService.listUsers(keyword, status, role, pageable));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/admin/users/{userId}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{userId}")
    public ResponseEntity<AdminApiResponse<AdminUserResponse>> getUserDetail(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(userService.getUserDetail(userId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/admin/users/{userId}/suspend
    // Body: { "reason": "Violated terms", "internalNote": "..." }
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{userId}/suspend")
    public ResponseEntity<AdminApiResponse<AdminUserResponse>> suspendUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminActionRequest req
    ) {
        return ResponseEntity.ok(userService.suspendUser(userId, req));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/admin/users/{userId}/activate
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{userId}/activate")
    public ResponseEntity<AdminApiResponse<AdminUserResponse>> activateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminActionRequest req
    ) {
        return ResponseEntity.ok(userService.activateUser(userId, req));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/admin/users/{userId}/promote
    // Elevate a user to ADMIN role
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{userId}/promote")
    public ResponseEntity<AdminApiResponse<AdminUserResponse>> promoteToAdmin(
            @PathVariable Long userId,
            @Valid @RequestBody AdminActionRequest req
    ) {
        return ResponseEntity.ok(userService.promoteToAdmin(userId, req));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/admin/users/{userId}/demote
    // Demote an admin back to regular USER
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{userId}/demote")
    public ResponseEntity<AdminApiResponse<AdminUserResponse>> demoteToUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminActionRequest req
    ) {
        return ResponseEntity.ok(userService.demoteToUser(userId, req));
    }


    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/admin/users/{userId}
    // Permanently remove a user (cascades to their data via JPA)
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{userId}")
    public ResponseEntity<AdminApiResponse<Void>> deleteUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminActionRequest req
    ) {
        return ResponseEntity.ok(userService.deleteUser(userId, req));
    }
}
