package com.finalyear.liwatch.rating.repository;

import com.finalyear.liwatch.rating.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    @Query("SELECT r FROM Rating r WHERE r.barter.id = :barterId AND r.fromUser.id = :fromUserId")
    Optional<Rating> findByBarterIdAndFromUserId(@Param("barterId") Long barterId,
                                                  @Param("fromUserId") Long fromUserId);

    @Query("SELECT r FROM Rating r WHERE r.barter.id = :barterId")
    List<Rating> findByBarterId(@Param("barterId") Long barterId);

    @Query("SELECT r FROM Rating r WHERE r.toUser.id = :toUserId AND r.isPublished = true ORDER BY r.publishedAt DESC")
    List<Rating> findPublishedByToUserId(@Param("toUserId") Long toUserId);

    @Query("SELECT COALESCE(AVG(r.score), 0.0) FROM Rating r WHERE r.toUser.id = :userId AND r.isPublished = true")
    Double averagePublishedScoreForUser(@Param("userId") Long userId);
}
