package com.finalyear.liwatch.test_repositories;

import com.finalyear.liwatch.directswap.DirectSwapRequest;
import com.finalyear.liwatch.negotiation.Negotiation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DirectSwapRequestRepository extends JpaRepository<DirectSwapRequest,Long> {
    @Query("""
    SELECT r FROM DirectSwapRequest r
    JOIN FETCH r.sender
    JOIN FETCH r.receiver
    JOIN FETCH r.offeredPost
    JOIN FETCH r.requestedPost
    WHERE r.id = :id
    """)
    Optional<DirectSwapRequest> findFullRequest(Long id);
}
