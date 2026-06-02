package com.finalyear.liwatch.cycleswap.repository;

import com.finalyear.liwatch.cycleswap.model.CycleAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CycleAgreementRepository extends JpaRepository<CycleAgreement, Long> {
}
