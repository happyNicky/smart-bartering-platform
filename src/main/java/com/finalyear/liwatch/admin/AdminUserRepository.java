package com.finalyear.liwatch.admin;

import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.userManagement.utils.enums.Status;
import com.finalyear.liwatch.userManagement.utils.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<User, Long> {

    // ── List / search ──────────────────────────────────────────────────────────

    /**
     * Search users by name or email (case-insensitive), optionally filtered by
     * status and/or role. Passing null for status/role skips that filter.
     */
    @Query("""
            SELECT u FROM User u
            LEFT JOIN FETCH u.userProfile p
            WHERE (:keyword IS NULL
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR u.status = :status)
              AND (:role   IS NULL OR u.role   = :role)
            ORDER BY u.createdAt DESC
            """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("status")  Status status,
            @Param("role")    Role role,
            Pageable pageable
    );

    // ── Single user with full profile ─────────────────────────────────────────

    @Query("""
            SELECT u FROM User u
            LEFT JOIN FETCH u.userProfile
            WHERE u.id = :id
            """)
    Optional<User> findByIdWithProfile(@Param("id") Long id);

    // ── Stats counts ──────────────────────────────────────────────────────────

    long countByStatus(Status status);

    long countByIsVerified(boolean verified);

    long countByCreatedAtAfter(LocalDateTime since);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'USER'")
    long countNonAdminUsers();

    // ── Post count per user (used in activity summary) ────────────────────────

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId")
    int countPostsByUserId(@Param("userId") Long userId);

    // ── Barter count per user ─────────────────────────────────────────────────

    @Query("""
            SELECT COUNT(b) FROM Barter b
            WHERE b.userA.id = :userId OR b.userB.id = :userId
            """)
    int countBartersByUserId(@Param("userId") Long userId);

    // ── Report count against a user ───────────────────────────────────────────

    @Query("SELECT COUNT(r) FROM UserReport r WHERE r.reportedUser.id = :userId")
    int countReportsByUserId(@Param("userId") Long userId);
}
