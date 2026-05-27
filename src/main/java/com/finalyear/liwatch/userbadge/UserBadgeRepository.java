package com.finalyear.liwatch.userbadge;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findByUserIdOrderByAwardedAtDesc(Long userId);
}
