package com.finalyear.liwatch.admin;

import com.finalyear.liwatch.Post.Post;
import com.finalyear.liwatch.Post.enums.Status;
import com.finalyear.liwatch.Post.enums.PostType;
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
            @Param("status")   Status status,
            @Param("postType") PostType postType,
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

    @Query("SELECT COUNT(r) FROM DirectSwapRequest r WHERE r.offeredPost.postId   = :postId OR r.requestedPost.postId = :postId")
    int countSwapRequestsForPost(@Param("postId") Long postId);

    @Query("SELECT COUNT(b) FROM Barter b WHERE b.postA.postId = :postId OR b.postB.postId = :postId")
    int countBartersForPost(@Param("postId") Long postId);

    @Query("SELECT COUNT(r) FROM UserReport r WHERE r.reportedPost.postId = :postId")
    int countReportsByPostId(@Param("postId") Long postId);

    // ── Stats ─────────────────────────────────────────────────────────────────

    long countByStatus(Status status);

    @Query("SELECT COUNT(p) FROM Post p WHERE TYPE(p) = com.finalyear.liwatch.Item.Item")
    long countItems();

    @Query("SELECT COUNT(p) FROM Post p WHERE TYPE(p) = com.finalyear.liwatch.service.Service")
    long countServices();

    long countByCreatedAtAfter(LocalDateTime since);
}
