package com.finalyear.liwatch.community_group;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupPostRepository extends JpaRepository<GroupPost, Long> {

    Optional<GroupPost> findByGroupGroupIdAndPostPostId(Long groupId, Long postId);

    boolean existsByGroupGroupIdAndPostPostId(Long groupId, Long postId);

    @Query("SELECT gp FROM GroupPost gp JOIN gp.post p JOIN p.user u WHERE gp.group.groupId = :groupId " +
           "AND p.status = com.finalyear.liwatch.Post.enums.Status.ACTIVE " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(p.category) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:category IS NULL OR :category = '' OR LOWER(p.category) = LOWER(:category)) " +
           "AND (:location IS NULL OR :location = '' OR LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%')))")
    List<GroupPost> searchGroupPosts(
            @Param("groupId") Long groupId,
            @Param("search") String search,
            @Param("category") String category,
            @Param("location") String location,
            Sort sort);
}
