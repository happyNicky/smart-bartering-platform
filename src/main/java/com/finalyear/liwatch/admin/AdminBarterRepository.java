package com.finalyear.liwatch.admin;

import com.finalyear.liwatch.barter.Barter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminBarterRepository extends JpaRepository<Barter, Long> {

    // ── List / search ──────────────────────────────────────────────────────────

    /**
     * Search barters, optionally filtered by negotiation status.
     * Fetches both users and both posts eagerly to avoid N+1 in DTO mapping.
     */
    @Query("""
            SELECT b FROM Barter b
            LEFT JOIN FETCH b.userA
            LEFT JOIN FETCH b.userB
            LEFT JOIN FETCH b.postA
            LEFT JOIN FETCH b.postB
            LEFT JOIN FETCH b.negotiation n
            WHERE (:negotiationStatus IS NULL
                   OR n.status = :negotiationStatus)
              AND (:keyword IS NULL
                   OR LOWER(b.userA.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.userB.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.postA.title)    LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.postB.title)    LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY b.createdAt DESC
            """)
    Page<Barter> searchBarters(
            @Param("negotiationStatus") String negotiationStatus,
            @Param("keyword")           String keyword,
            Pageable pageable
    );

    /**
     * All barters involving a specific user (as either party).
     */
    @Query("""
            SELECT b FROM Barter b
            LEFT JOIN FETCH b.userA
            LEFT JOIN FETCH b.userB
            LEFT JOIN FETCH b.postA
            LEFT JOIN FETCH b.postB
            LEFT JOIN FETCH b.negotiation
            WHERE b.userA.id = :userId OR b.userB.id = :userId
            ORDER BY b.createdAt DESC
            """)
    Page<Barter> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Single barter with everything loaded — users, posts, negotiation, agreements.
     */
    @Query("""
            SELECT b FROM Barter b
            LEFT JOIN FETCH b.userA
            LEFT JOIN FETCH b.userB
            LEFT JOIN FETCH b.postA
            LEFT JOIN FETCH b.postB
            LEFT JOIN FETCH b.negotiation n
            LEFT JOIN FETCH b.agreements
            LEFT JOIN FETCH b.swapRequest
            WHERE b.id = :barterId
            """)
    Optional<Barter> findByIdWithDetails(@Param("barterId") Long barterId);

    // ── Stats ─────────────────────────────────────────────────────────────────

    @Query("""
            SELECT COUNT(b) FROM Barter b
            LEFT JOIN b.negotiation n
            WHERE n.status = 'AGREED'
            """)
    long countCompleted();

    @Query("""
            SELECT COUNT(b) FROM Barter b
            LEFT JOIN b.negotiation n
            WHERE n.status = 'PENDING'
            """)
    long countActive();
}
