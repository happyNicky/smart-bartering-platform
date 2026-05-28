package com.finalyear.liwatch.admin;

import com.finalyear.liwatch.Post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AdminPostRepository extends JpaRepository<Post, Long> {

    // ── List / search ──────────────────────────────────────────────────────────

    /**
     * Search posts by title keyword, optionally filtered by status and/or postType.
     * Fetches owner (user) eagerly to avoid N+1 when mapping to DTOs.
     */
    @Query("""
            SELECT p FROM Post p
            LEFT JOIN FETCH p.user u
            WHERE (:keyword IS NULL
                   OR LOWER(p.title)    LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status   IS NULL OR p.status   = :status)
              AND (:postType IS NULL OR p.postType = :postType)
            ORDER BY p.createdAt DESC
            """)
    Page<Post> searchPosts(
            @Param("keyword")  String keyword,
            @Param("status")   String status,
            @Param("postType") String postType,
            Pageable pageable
    );

    /**
     * Posts owned by a specific user — useful for "view all listings of user X".
     */
    @Query("""
            SELECT p FROM Post p
            LEFT JOIN FETCH p.user
            WHERE p.user.id = :userId
            ORDER BY p.createdAt DESC
            """)
    Page<Post> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Fetch single post with its owner and media list fully loaded.
     */
    @Query("""
            SELECT p FROM Post p
            LEFT JOIN FETCH p.user
            LEFT JOIN FETCH p.postImages
            WHERE p.postId = :postId
            """)
    Optional<Post> findByIdWithDetails(@Param("postId") Long postId);

    // ── Counts for reports linked to a post ────────────────────────────────────
    // UserReport is user-to-user, so we count swap requests on this post
    // as a proxy for activity; real "post reports" can be added later.

    @Query("SELECT COUNT(r) FROM DirectSwapRequest r WHERE r.offeredPost.postId   = :postId OR r.requestedPost.postId = :postId")
    int countSwapRequestsForPost(@Param("postId") Long postId);

    // ── Stats ─────────────────────────────────────────────────────────────────

    long countByStatus(com.finalyear.liwatch.Post.enums.Status status);

    @Query("SELECT COUNT(p) FROM Post p WHERE TYPE(p) = com.finalyear.liwatch.Post.Item")
    long countItems();

    @Query("SELECT COUNT(p) FROM Post p WHERE TYPE(p) = com.finalyear.liwatch.Post.Service")
    long countServices();

    long countByCreatedAtAfter(LocalDateTime since);
}
