package com.finalyear.liwatch.community_group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityGroupRepository extends JpaRepository<CommunityGroup, Long> {

    @Query("SELECT g FROM CommunityGroup g WHERE g.status = :status " +
           "AND (:location IS NULL OR :location = '' OR g.location = :location) " +
           "AND (:category IS NULL OR :category = '' OR g.category = :category) " +
           "AND (:q IS NULL OR :q = '' OR LOWER(g.groupName) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<CommunityGroup> searchGroups(@Param("status") CommunityGroup.Status status,
                                      @Param("location") String location,
                                      @Param("category") String category,
                                      @Param("q") String q);
}
