package com.finalyear.liwatch.negotiation.negotiation_management;

import com.finalyear.liwatch.barter.Barter;
import com.finalyear.liwatch.negotiation.Negotiation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NegotiationRepository extends JpaRepository<Negotiation,Long> {
    List<Negotiation> findByBarter(Barter barter);
    // Inside NegotiationRepository.java
    @Query ("""
    SELECT DISTINCT n FROM Negotiation n 
    JOIN FETCH n.barter b 
    LEFT JOIN FETCH n.messages m
    WHERE b.userA.id = :userId OR b.userB.id = :userId
""")
    List<Negotiation> findUserNegotiations(@Param("userId") Long userId);


}
