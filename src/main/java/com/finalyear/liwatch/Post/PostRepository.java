package com.finalyear.liwatch.Post;

import com.finalyear.liwatch.userManagement.model.User;
import com.finalyear.liwatch.Post.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
    Page<Post> findByCategory(String category, Pageable pageable);
    Page<Post> findByUser(User user, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE (p.isGroupOnly IS NULL OR p.isGroupOnly = false) AND (p.isVisible IS NULL OR p.isVisible = true) AND p.status = com.finalyear.liwatch.Post.enums.Status.ACTIVE")
    Page<Post> findAllGlobal(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.category = :category AND (p.isGroupOnly IS NULL OR p.isGroupOnly = false) AND (p.isVisible IS NULL OR p.isVisible = true) AND p.status = com.finalyear.liwatch.Post.enums.Status.ACTIVE")
    Page<Post> findByCategoryGlobal(@Param("category") String category, Pageable pageable);

    List<Post> findByGroupId(Long groupId);

    List<Post> findByGroupIdAndStatus(Long groupId, Status status);
}
