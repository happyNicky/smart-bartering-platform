package com.finalyear.liwatch.admin;


import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.Post.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPostService {

    private final AdminPostRepository postRepo;

    // ── List / search all posts ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminApiResponse<List<AdminPostResponse>> listPosts(
            String keyword,
            String status,
            String postType,
            Pageable pageable) {

        Page<Post> page = postRepo.searchPosts(keyword, status, postType, pageable);

        Page<AdminPostResponse> mapped = page.map(post -> {
            AdminPostResponse dto = AdminPostResponse.from(post);
            dto.setReportCount(postRepo.countSwapRequestsForPost(post.getPostId()));
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
        dto.setReportCount(postRepo.countSwapRequestsForPost(postId));
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

        log.info("[ADMIN] Approved post {} — reason: {}", postId, req.getReason());
        return AdminApiResponse.ok(AdminPostResponse.from(post), "Post approved.");
    }

    // ── Remove / take down a post (set status → REMOVED) ─────────────────────

    @Transactional
    public AdminApiResponse<AdminPostResponse> removePost(Long postId, AdminActionRequest req) {
        Post post = findOrThrow(postId);

        postRepo.delete(post);

        log.warn("[ADMIN] Removed post {} (owner: {}) — reason: {}",
                postId,
                post.getUser() != null ? post.getUser().getEmail() : "unknown",
                req.getReason());

        return AdminApiResponse.ok(AdminPostResponse.from(post), "Post removed.");
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

        log.info("[ADMIN] Flagged post {} for review — reason: {}", postId, req.getReason());
        return AdminApiResponse.ok(AdminPostResponse.from(post), "Post flagged for review.");
    }

    // ── Expire a post (set status → EXPIRED) ──────────────────────────────────

    @Transactional
    public AdminApiResponse<AdminPostResponse> expirePost(Long postId, AdminActionRequest req) {
        Post post = findOrThrow(postId);

        post.setStatus(Status.CLOSED);
        postRepo.save(post);

        log.info("[ADMIN] Expired post {} — reason: {}", postId, req.getReason());
        return AdminApiResponse.ok(AdminPostResponse.from(post), "Post expired.");
    }

    // ── Hard-delete a post ────────────────────────────────────────────────────

    @Transactional
    public AdminApiResponse<Void> deletePost(Long postId, AdminActionRequest req) {
        Post post = findOrThrow(postId);

        postRepo.delete(post);

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
