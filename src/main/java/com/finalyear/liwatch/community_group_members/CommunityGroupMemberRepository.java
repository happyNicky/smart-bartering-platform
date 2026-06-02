package com.finalyear.liwatch.community_group_members;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityGroupMemberRepository extends JpaRepository<CommunityGroupMember, Long> {
    Optional<CommunityGroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
    List<CommunityGroupMember> findByGroupId(Long groupId);
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    Long countByGroupId(Long groupId);
}
