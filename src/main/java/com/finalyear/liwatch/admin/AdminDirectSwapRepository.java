package com.finalyear.liwatch.admin;

import com.finalyear.liwatch.directswap.DirectSwapRequest;
import com.finalyear.liwatch.directswap.request_enum.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminDirectSwapRepository extends JpaRepository<DirectSwapRequest, Long> {

    /**
     * All swap requests, optionally filtered by status.
     * Fetches sender, receiver, and both posts eagerly.
     */
    @Query("""
            SELECT r FROM DirectSwapRequest r
            LEFT JOIN FETCH r.requestSender
            LEFT JOIN FETCH r.requestReceiver
            LEFT JOIN FETCH r.offeredPost
            LEFT JOIN FETCH r.requestedPost
            WHERE (:status IS NULL OR r.status = :status)
            ORDER BY r.createdAt DESC
            """)
    Page<DirectSwapRequest> searchRequests(
            @Param("status") String status,
            Pageable pageable
    );

    /**
     * Single swap request with all associations loaded.
     */
    @Query("""
            SELECT r FROM DirectSwapRequest r
            LEFT JOIN FETCH r.requestSender
            LEFT JOIN FETCH r.requestReceiver
            LEFT JOIN FETCH r.offeredPost
            LEFT JOIN FETCH r.requestedPost
            LEFT JOIN FETCH r.barter
            WHERE r.id = :requestId
            """)
    Optional<DirectSwapRequest> findByIdWithDetails(@Param("requestId") Long requestId);

    // ── Stats ─────────────────────────────────────────────────────────────────

    long countByStatus(RequestStatus status);
}
