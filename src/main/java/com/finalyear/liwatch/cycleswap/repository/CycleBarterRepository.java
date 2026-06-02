package com.finalyear.liwatch.cycleswap.repository;

import com.finalyear.liwatch.cycleswap.model.CycleBarter;
import com.finalyear.liwatch.userManagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CycleBarterRepository extends JpaRepository<CycleBarter, Long> {
    List<CycleBarter> findByUserAOrUserBOrUserC(User userA, User userB, User userC);
}
