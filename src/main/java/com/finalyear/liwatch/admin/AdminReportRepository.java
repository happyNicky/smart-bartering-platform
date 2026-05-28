package com.finalyear.liwatch.admin;

import com.finalyear.liwatch.report.UserReport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminReportRepository extends JpaRepository<UserReport, Long> {

    // ── List / search ──────────────────────────────────────────────────────────

    /**
     * Search reports filtered by status, and optionally by the reported user's
     * name or email. Fetches both users eagerly to avoid N+1 in DTO mapping.
     */
    @Query("""
            SELECT r FROM UserReport r
            LEFT JOIN FETCH r.reportedUser ru
            LEFT JOIN FETCH r.reporterUser rp
            WHERE (:status IS NULL OR r.status = :status)
              AND (:keyword IS NULL
                   OR LOWER(ru.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(ru.email)    LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(rp.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY r.createdAt DESC
            """)
    Page<UserReport> searchReports(
            @Param("status")  String status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * All reports filed against a specific user — useful when viewing
     * a user's profile in the admin panel.
     */
    @Query("""
            SELECT r FROM UserReport r
            LEFT JOIN FETCH r.reporterUser
            WHERE r.reportedUser.id = :userId
            ORDER BY r.createdAt DESC
            """)
    Page<UserReport> findByReportedUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * All reports filed BY a specific user.
     */
    @Query("""
            SELECT r FROM UserReport r
            LEFT JOIN FETCH r.reportedUser
            WHERE r.reporterUser.id = :userId
            ORDER BY r.createdAt DESC
            """)
    Page<UserReport> findByReporterUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * Single report with both users fully loaded.
     */
    @Query("""
            SELECT r FROM UserReport r
            LEFT JOIN FETCH r.reportedUser
            LEFT JOIN FETCH r.reporterUser
            WHERE r.reportId = :reportId
            """)
    Optional<UserReport> findByIdWithUsers(@Param("reportId") Long reportId);

    // ── Stats ─────────────────────────────────────────────────────────────────

    long countByStatus(ReportStatus status);
}
