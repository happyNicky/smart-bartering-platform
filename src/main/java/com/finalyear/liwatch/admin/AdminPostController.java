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
 * Admin endpoints for post / listing moderation.
 *
 * Base path: /api/admin/posts
 *
 * Status lifecycle an admin can drive:
 *   PENDING → ACTIVE   (approve)
 *   ACTIVE  → FLAGGED  (flag for review)
 *   FLAGGED → ACTIVE   (approve after review)
 *   any     → REMOVED  (take down)
 *   any     → EXPIRED  (force-expire)
 *   any     → DELETE   (hard delete)
 */
@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
@AdminOnly
public class AdminPostController {

    private final AdminPostService postService;

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/admin/posts
    //   ?keyword=bike&status=FLAGGED&postType=ITEM&page=0&size=20
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<AdminApiResponse<List<AdminPostResponse>>> listPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String postType,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(postService.listPosts(keyword, status, postType, pageable));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/admin/posts/user/{userId}
    // All listings belonging to a specific user
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/user/{userId}")
    public ResponseEntity<AdminApiResponse<List<AdminPostResponse>>> listPostsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(postService.listPostsByUser(userId, pageable));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/admin/posts/{postId}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{postId}")
    public ResponseEntity<AdminApiResponse<AdminPostResponse>> getPostDetail(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(postService.getPostDetail(postId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/admin/posts/{postId}/approve
    // Sets status → ACTIVE (use after reviewing a flagged or pending post)
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{postId}/approve")
    public ResponseEntity<AdminApiResponse<AdminPostResponse>> approvePost(
            @PathVariable Long postId,
            @Valid @RequestBody AdminActionRequest req
    ) {
        return ResponseEntity.ok(postService.approvePost(postId, req));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/admin/posts/{postId}/remove
    // Sets status → REMOVED (soft take-down, keeps record in DB)
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{postId}/remove")
    public ResponseEntity<AdminApiResponse<AdminPostResponse>> removePost(
            @PathVariable Long postId,
            @Valid @RequestBody AdminActionRequest req
    ) {
        return ResponseEntity.ok(postService.removePost(postId, req));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/admin/posts/{postId}/flag
    // Sets status → FLAGGED (puts post in moderation queue)
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{postId}/flag")
    public ResponseEntity<AdminApiResponse<AdminPostResponse>> flagPost(
            @PathVariable Long postId,
            @Valid @RequestBody AdminActionRequest req
    ) {
        return ResponseEntity.ok(postService.flagPost(postId, req));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/admin/posts/{postId}/expire
    // Sets status → EXPIRED (admin-forced expiry)
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{postId}/close")
    public ResponseEntity<AdminApiResponse<AdminPostResponse>> expirePost(
            @PathVariable Long postId,
            @Valid @RequestBody AdminActionRequest req
    ) {
        return ResponseEntity.ok(postService.expirePost(postId, req));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/admin/posts/{postId}
    // Hard delete — removes the record from the database permanently
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{postId}")
    public ResponseEntity<AdminApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @Valid @RequestBody AdminActionRequest req
    ) {
        return ResponseEntity.ok(postService.deletePost(postId, req));
    }
}
