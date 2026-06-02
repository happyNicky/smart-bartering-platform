package com.finalyear.liwatch.admin;


import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.Post.enums.Status;
import com.finalyear.liwatch.Post.enums.PostType;
import com.finalyear.liwatch.Notification.NotificationService;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPostService {

    private final AdminPostRepository postRepo;
    private final AdminActionLogRepository logRepo;
    private final UserUtilService userUtil;
    private final NotificationService notificationService;

    // ── List / search all posts ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminApiResponse<List<AdminPostResponse>> listPosts(
            String keyword,
            String statusStr,
            String postTypeStr,
            Pageable pageable) {

        Status status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try { status = Status.valueOf(statusStr.toUpperCase()); } catch (Exception e) {}
        }

        PostType postType = null;
        if (postTypeStr != null && !postTypeStr.isBlank()) {
            try { postType = PostType.valueOf(postTypeStr.toUpperCase()); } catch (Exception e) {}
        }

        Page<Post> page = postRepo.searchPosts(keyword, status, postType, pageable);

        Page<AdminPostResponse> mapped = page.map(post -> {
            AdminPostResponse dto = AdminPostResponse.from(post);
            dto.setReportCount(postRepo.countReportsByPostId(post.getPostId()));
            return dto;
        });

        return AdminApiResponse.ofPage(mapped);
    }

    // ── All posts by a specific user ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminApiResponse<List<AdminPostResponse>> listPostsByUser(
            Long userId, Pageable pageable) {

        Page<AdminPostResponse> mapped = postRepo
                .findByUserId(userId, pageable)
                .map(AdminPostResponse::from);

        return AdminApiResponse.ofPage(mapped);
    }

    // ── Single post detail ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminApiResponse<AdminPostResponse> getPostDetail(Long postId) {
        Post post = findOrThrow(postId);
        AdminPostResponse dto = AdminPostResponse.from(post);
        dto.setReportCount(postRepo.countReportsByPostId(postId));
        return AdminApiResponse.ok(dto);
    }

    // ── Approve a post (set status → ACTIVE) ──────────────────────────────────

    @Transactional
    public AdminApiResponse<AdminPostResponse> approvePost(Long postId, AdminActionRequest req) {
        Post post = findOrThrow(postId);

        if (post.getStatus() == Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Post is already active.");
        }

        post.setStatus(Status.ACTIVE);
        postRepo.save(post);

        // Notify the post owner
        if (post.getUser() != null) {
            try {
                notificationService.createNotification(
                        post.getUser().getId(),
                        post.getUser().getEmail(),
                        "Post Approved",
                        "Your post '" + post.getTitle() + "' has been approved by the administrator.",
                        "POST_APPROVED"
                );
            } catch (Exception e) {
                log.error("Failed to send post approval notification: {}", e.getMessage());
            }
        }

        try {
            String adminEmail = userUtil.getCurrentlyAuthenticatedUser().getEmail();
            logRepo.save(AdminActionLog.builder()
                    .adminEmail(adminEmail)
                    .actionType("APPROVE_POST")
                    .targetType("POST")
                    .targetId(postId)
                    .reason(req.getReason())
                    .actionTime(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to save admin action log: {}", e.getMessage());
        }

        log.info("[ADMIN] Approved post {} — reason: {}", postId, req.getReason());
        return AdminApiResponse.ok(AdminPostResponse.from(post), "Post approved.");
    }

    // ── Remove / take down a post (set status → REMOVED or hard-delete) ──────

    @Transactional
    public AdminApiResponse<AdminPostResponse> removePost(Long postId, AdminActionRequest req) {
        Post post = findOrThrow(postId);

        int swapRequestsCount = postRepo.countSwapRequestsForPost(postId);
        int bartersCount = postRepo.countBartersForPost(postId);
        int reportsCount = postRepo.countReportsByPostId(postId);

        boolean hasReferences = swapRequestsCount > 0 || bartersCount > 0 || reportsCount > 0;
        AdminPostResponse responseDto = AdminPostResponse.from(post);
        String actionType;

        if (hasReferences) {
            post.setStatus(Status.REMOVED);
            postRepo.save(post);
            actionType = "REMOVE_POST";
            log.warn("[ADMIN] Soft-removed post {} (owner: {}) — reason: {}",
                    postId,
                    post.getUser() != null ? post.getUser().getEmail() : "unknown",
                    req.getReason());
        } else {
            postRepo.delete(post);
            actionType = "DELETE_POST";
            log.warn("[ADMIN] Hard-deleted post {} since it has no references — reason: {}",
                    postId,
                    req.getReason());
        }

        // Notify the post owner
        if (post.getUser() != null) {
            try {
                notificationService.createNotification(
                        post.getUser().getId(),
                        post.getUser().getEmail(),
                        "Post Removed",
                        "Your post '" + post.getTitle() + "' has been removed by the administrator. Reason: " + req.getReason(),
                        "POST_REMOVED"
                );
            } catch (Exception e) {
                log.error("Failed to send post removal notification: {}", e.getMessage());
            }
        }

        try {
            String adminEmail = userUtil.getCurrentlyAuthenticatedUser().getEmail();
            logRepo.save(AdminActionLog.builder()
                    .adminEmail(adminEmail)
                    .actionType(actionType)
                    .targetType("POST")
                    .targetId(postId)
                    .reason(req.getReason())
                    .actionTime(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to save admin action log: {}", e.getMessage());
        }

        String msg = hasReferences 
                ? "Post has active swap requests, barters, or reports; it was flagged 'REMOVED'."
                : "Post has been completely deleted from the database.";

        return AdminApiResponse.ok(responseDto, msg);
    }

    // ── Flag a post for review ────────────────────────────────────────────────
    // Sets status → FLAGGED so it sits in a moderation queue.

    @Transactional
    public AdminApiResponse<AdminPostResponse> flagPost(Long postId, AdminActionRequest req) {
        Post post = findOrThrow(postId);

        if (post.getStatus() == Status.FLAGGED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Post is already flagged.");
        }

        post.setStatus(Status.FLAGGED);
        postRepo.save(post);

        // Notify the post owner
        if (post.getUser() != null) {
            try {
                notificationService.createNotification(
                        post.getUser().getId(),
                        post.getUser().getEmail(),
                        "Post Flagged",
                        "Your post '" + post.getTitle() + "' has been flagged for review by the administrator. Reason: " + req.getReason(),
                        "POST_FLAGGED"
                );
            } catch (Exception e) {
                log.error("Failed to send post flagging notification: {}", e.getMessage());
            }
        }

        try {
            String adminEmail = userUtil.getCurrentlyAuthenticatedUser().getEmail();
            logRepo.save(AdminActionLog.builder()
                    .adminEmail(adminEmail)
                    .actionType("FLAG_POST")
                    .targetType("POST")
                    .targetId(postId)
                    .reason(req.getReason())
                    .actionTime(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to save admin action log: {}", e.getMessage());
        }

        log.info("[ADMIN] Flagged post {} for review — reason: {}", postId, req.getReason());
        return AdminApiResponse.ok(AdminPostResponse.from(post), "Post flagged for review.");
    }

    // ── Expire a post (set status → EXPIRED) ──────────────────────────────────

    @Transactional
    public AdminApiResponse<AdminPostResponse> expirePost(Long postId, AdminActionRequest req) {
        Post post = findOrThrow(postId);

        post.setStatus(Status.CLOSED);
        postRepo.save(post);

        try {
            String adminEmail = userUtil.getCurrentlyAuthenticatedUser().getEmail();
            logRepo.save(AdminActionLog.builder()
                    .adminEmail(adminEmail)
                    .actionType("CLOSE_POST")
                    .targetType("POST")
                    .targetId(postId)
                    .reason(req.getReason())
                    .actionTime(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to save admin action log: {}", e.getMessage());
        }

        log.info("[ADMIN] Expired post {} — reason: {}", postId, req.getReason());
        return AdminApiResponse.ok(AdminPostResponse.from(post), "Post expired.");
    }

    // ── Hard-delete a post ────────────────────────────────────────────────────

    @Transactional
    public AdminApiResponse<Void> deletePost(Long postId, AdminActionRequest req) {
        Post post = findOrThrow(postId);

        postRepo.delete(post);

        try {
            String adminEmail = userUtil.getCurrentlyAuthenticatedUser().getEmail();
            logRepo.save(AdminActionLog.builder()
                    .adminEmail(adminEmail)
                    .actionType("DELETE_POST")
                    .targetType("POST")
                    .targetId(postId)
                    .reason(req.getReason())
                    .actionTime(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to save admin action log: {}", e.getMessage());
        }

        log.warn("[ADMIN] DELETED post {} permanently — reason: {}", postId, req.getReason());
        return AdminApiResponse.ok(null, "Post deleted permanently.");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Post findOrThrow(Long postId) {
        return postRepo.findByIdWithDetails(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Post not found: id=" + postId));
    }
}
