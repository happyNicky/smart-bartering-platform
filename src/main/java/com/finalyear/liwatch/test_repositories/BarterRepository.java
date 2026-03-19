package com.finalyear.liwatch.test_repositories;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.negotiation.Negotiation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BarterRepository extends JpaRepository<Barter,Long> {
    @Query("""
    SELECT b FROM Barter b
    JOIN FETCH b.swapRequest sr
    JOIN FETCH sr.sender
    JOIN FETCH sr.receiver
    JOIN FETCH b.negotiation n
    LEFT JOIN FETCH n.messages
    WHERE b.id = :id
    """)
        Optional<Barter> findFullBarter(Long id);
}
