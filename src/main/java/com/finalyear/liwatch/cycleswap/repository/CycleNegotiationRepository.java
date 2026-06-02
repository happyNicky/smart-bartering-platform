package com.finalyear.liwatch.cycleswap.repository;

import com.finalyear.liwatch.cycleswap.model.CycleNegotiation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CycleNegotiationRepository extends JpaRepository<CycleNegotiation, Long> {
}
