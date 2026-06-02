package com.finalyear.liwatch.cycleswap.repository;

import com.finalyear.liwatch.cycleswap.model.CycleSwapRequest;
import com.finalyear.liwatch.userManagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CycleSwapRequestRepository extends JpaRepository<CycleSwapRequest, Long> {
    List<CycleSwapRequest> findByInitiatorOrMiddlemanOrCloser(User initiator, User middleman, User closer);
}
