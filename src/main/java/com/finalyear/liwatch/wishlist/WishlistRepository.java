package com.finalyear.liwatch.wishlist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserId(Long userId);
    Optional<Wishlist> findByUserIdAndPostPostId(Long userId, Long postId);
    boolean existsByUserIdAndPostPostId(Long userId, Long postId);
    void deleteByUserIdAndPostPostId(Long userId, Long postId);
}
