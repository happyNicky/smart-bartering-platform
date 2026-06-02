package com.finalyear.liwatch.cycleswap.repository;

import com.finalyear.liwatch.cycleswap.model.CycleChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CycleChatRepository extends JpaRepository<CycleChat, Long> {
}
